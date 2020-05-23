package com.haizhi.iap.follow.controller;

import com.google.common.base.Strings;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.follow.controller.model.CompanyImportAck;
import com.haizhi.iap.follow.controller.model.FileView;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.service.CompanyImportService;
import com.haizhi.iap.follow.utils.MultipartFileUtil;
import io.swagger.annotations.Api;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by chenbo on 17/1/9.
 */
@Api(tags="【关注-导入模块】导入企业信息")
@RestController
@RequestMapping(value = "/follow/import")
public class ImportController {

    @Setter
    @Autowired
    CompanyImportService companyImportService;

    /**
     * multipart/form-data 上传
     * @return
     */
    @RequestMapping(value = "/batch_import_company", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    public Wrapper batchImportCompany(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            return Wrapper.ERRORBuilder.data(e.getMessage()).build();
        }

        String extention = MultipartFileUtil.readExtension(filename);

        CompanyImportAck ack;
        if (extention.equals(MultipartFileUtil.TXT)) {
            ack = companyImportService.importFromTXT(data);
        } else if (extention.equals(MultipartFileUtil.XLS) || extention.equals(MultipartFileUtil.XLSX)) {
            ack = companyImportService.importFromExcel(data);
        } else {
            return FollowException.UNSUPPORT_FILE.get();
        }
        ack.setItems(null);
        return Wrapper.OKBuilder.data(ack).build();
    }

    /**
     * base64上传
     *
     * @param fileView
     * @return
     */
    @RequestMapping(value = "/batch_import_company", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper batchImportCompany(@RequestBody FileView fileView) {
        //TODO
        return Wrapper.OK;
    }

    @RequestMapping(value = "/batch_import_company_commit", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper batchImportCompanyCommit(@RequestBody CompanyImportAck ack) {
        if (ack == null) {
            return FollowException.MISS_BODY.get();
        }

        if (Strings.isNullOrEmpty(ack.getCacheKey())) {
            return FollowException.NO_CACHE_KEY.get();
        }
        if (ack.getFollowListId() == null) {
            return FollowException.NO_GROUP_ID.get();
        }
        companyImportService.commit(ack.getCacheKey(), ack.getFollowListId(), DefaultSecurityContext.getUserId());
        return Wrapper.OK;
    }


}
