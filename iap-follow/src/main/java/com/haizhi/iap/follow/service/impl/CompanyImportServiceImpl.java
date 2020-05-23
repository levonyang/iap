package com.haizhi.iap.follow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.EncodingDetect;
import com.haizhi.iap.follow.controller.model.CompanyImportAck;
import com.haizhi.iap.follow.controller.model.CompanyImportItem;
import com.haizhi.iap.follow.enums.LimitConfig;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.FollowItem;
import com.haizhi.iap.follow.model.FollowList;
import com.haizhi.iap.follow.repo.FollowListRepo;
import com.haizhi.iap.follow.repo.RedisRepo;
import com.haizhi.iap.follow.repo.UserRepo;
import com.haizhi.iap.follow.service.CompanyImportService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenbo on 17/1/9.
 */
@Slf4j
@Service
public class CompanyImportServiceImpl implements CompanyImportService {

    private static final Integer DEFAULT_PRIORITY = 3;
    private static Pattern nameAndPrioPattern = Pattern.compile("^([^\\s]+)\\s+([0-9]+)\\s*$");
    private static Pattern namePattern = Pattern.compile("^([^\\s]+)\\s*$");

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Setter
    @Autowired
    UserRepo userRepo;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    FollowListRepo listRepo;

    @Setter
    @Autowired
    @Qualifier("producerTemplate")
    private ProducerTemplate template;

    @Override
    public CompanyImportAck importFromTXT(byte[] data) {

        List<CompanyImportItem> items = parseFromTXT(data);

        return generateAck(items);
    }

    @Override
    public CompanyImportAck importFromExcel(byte[] data) {
        List<CompanyImportItem> items = parseFromExcel(data);

        return generateAck(items);
    }

    @Override
    public void commit(String cacheKey, Long followListId, Long userId) {
        CompanyImportAck ack = redisRepo.getImportCache(cacheKey);
        FollowList list = listRepo.findById(followListId);
        if(list == null){
            throw new ServiceAccessException(FollowException.FOLLOW_LIST_NOT_EXIST);
        }
        Integer count = list.getListCount();
        if (ack == null) {
            throw new ServiceAccessException(FollowException.OPERATION_TIMEOUT);
        }else if(ack.getItems().size() + count > LimitConfig.ITEM_NUM_PER_LIST){
            throw new ServiceAccessException(FollowException.OVER_LIMIT_ITEM_NUM_PER_LIST);
        }else if(ack.getItems().size() + listRepo.sumItemCount(userId) > LimitConfig.ITEM_SUM_PER_USER){
            throw new ServiceAccessException(FollowException.OVER_LIMIT_ITEM_SUM_PER_USER);
        }
        List<FollowItem> items = Lists.newArrayList();
        List<Map<String, Object>> crawData = Lists.newArrayList();

        for (CompanyImportItem item : ack.getItems()) {
            FollowItem notExistItem = new FollowItem();
            notExistItem.setCompanyName(item.getName());
            notExistItem.setUserId(userId);
            notExistItem.setFollowListId(followListId);
            notExistItem.setIsExistsIn(0);
            items.add(notExistItem);

            Map<String, Object> data = Maps.newHashMap();
            data.put("company", item.getName());
            data.put("level", item.getPriority());
            crawData.add(data);
        }

        log.info("import total size :{}", items.size());

        template.asyncRequestBody("direct:batch_insert", items);

        //所有导入的公司向抓取系统的schedule接口发出请求
        template.asyncRequestBody("direct:batch_craw", crawData);
    }

    public List<CompanyImportItem> parseFromExcel(byte[] data) {
        List<CompanyImportItem> items = Lists.newArrayList();
        int row = 0;
        try {
            //获取工作簿
            Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data));
            //获取第一张工作表
            Sheet sheet = wb.getSheetAt(0);

            //获取列和行数
            Integer rowNum = sheet.getLastRowNum();
            if (rowNum > LimitConfig.IMPORT) {
                throw new ServiceAccessException(FollowException.OVER_LIMIT_IMPORT);
            }
            for (; row <= rowNum; row++) {
                Row dataRow = sheet.getRow(row);

                if (dataRow == null) {
                    continue;
                }
                Cell nameCell = dataRow.getCell(0);
                Cell prioCell = dataRow.getCell(1);

                if (nameCell == null || nameCell.getStringCellValue() == null) {
                    throw new ServiceAccessException(-1, "上传失败, txt文件第" + (row + 1) + "行格式有误!");
                } else if (nameCell.getStringCellValue().startsWith("企业名")
                        || nameCell.getStringCellValue().trim().equals("")) {
                    continue;
                }

                CompanyImportItem item = new CompanyImportItem();

                if (namePattern.matcher(nameCell.getStringCellValue()).matches()) {
                    item.setName(nameCell.getStringCellValue());
                    Integer prio = (prioCell == null) ? null : Double.valueOf(prioCell.getNumericCellValue()).intValue();
                    item.setPriority((prio == null || prio > DEFAULT_PRIORITY || prio < 1) ? DEFAULT_PRIORITY : prio);
                }
                if (item != null && item.getName() != null && !item.getName().equals("")) {
                    items.add(item);
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new ServiceAccessException(-1, "上传失败, txt文件第" + (row + 1) + "行格式有误!");
        } catch (ServiceAccessException ex) {
            throw ex;
        } catch (Exception exx) {
            exx.printStackTrace();
        }

        return items;
    }

    public List<CompanyImportItem> parseFromTXT(byte[] data) {
        List<CompanyImportItem> items = Lists.newArrayList();
        try {
            String line;
            Integer lineNumber = 0;

            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), EncodingDetect.getJavaEncode(data)));

            while ((line = reader.readLine()) != null) {
                lineNumber += 1;

                Matcher nameAndPrioMatcher = nameAndPrioPattern.matcher(line);
                Matcher nameMatcher = namePattern.matcher(line);
                if (!nameAndPrioMatcher.matches() && !nameMatcher.matches()) {
                    throw new ServiceAccessException(-1, "上传失败, txt文件第" + lineNumber + "行格式有误!");
                }
                CompanyImportItem item = new CompanyImportItem();
                if (nameAndPrioMatcher.matches()) {
                    item.setName(line.split("\\s+")[0]);
                    Integer prio = Integer.valueOf(line.split("\\s+")[1]);
                    item.setPriority(prio > DEFAULT_PRIORITY || prio < 1 ? DEFAULT_PRIORITY : prio);
                } else {
                    item.setName(line.trim());
                    item.setPriority(DEFAULT_PRIORITY);
                }
                items.add(item);
                if (items.size() > LimitConfig.IMPORT) {
                    throw new ServiceAccessException(FollowException.OVER_LIMIT_IMPORT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    public CompanyImportAck generateAck(List<CompanyImportItem> items) {

        CompanyImportAck ack = new CompanyImportAck();
        Integer first = 0;
        Integer second = 0;
        Integer third = 0;
        for (CompanyImportItem item : items) {
            if (Strings.isNullOrEmpty(item.getName())) {
                continue;
            }

            if (item.getPriority().equals(1)) {
                first++;
            } else if (item.getPriority().equals(2)) {
                second++;
            } else {
                third++;
            }
        }

        ack.setItems(items);
        ack.setTotal((long) items.size());
        ack.setPriorityFirst(first);
        ack.setPrioritySecond(second);
        ack.setPriorityThird(third);

        //将notExitList缓存redis 12h,确认上传从redis取出存库
        String cacheKey = redisRepo.pushImportCache(ack);
        ack.setCacheKey(cacheKey);
        return ack;
    }

}
