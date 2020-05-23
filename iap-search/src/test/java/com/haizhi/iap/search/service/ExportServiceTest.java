package com.haizhi.iap.search.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by chenbo on 2017/10/16.
 */
//@Slf4j
public class ExportServiceTest {
/*
    ClassPathXmlApplicationContext context;
    ExportService exportService;

    @Before
    public void init() {
        context = new ClassPathXmlApplicationContext(
                "classpath:spring/applicationContext.xml",
                "classpath:spring/applicationContext-data.xml");
        exportService = context.getBean(ExportService.class);
    }

    @Test
    public void testExport() throws FileNotFoundException {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File home = fsv.getHomeDirectory();
        File desktopPath = new File(home.getAbsolutePath() + "/Desktop");
        FileOutputStream out = new FileOutputStream(desktopPath + "/test.pdf");
        if(desktopPath.isDirectory()){
            exportService.exportFinancialReport(out, "company_ability",
                    "600036", "2014-03-31", "一季", "招商银行股份有限公司");
            log.info("导出成功, file path: {}", desktopPath.getAbsolutePath());
        }else {
            log.warn("当前系统没有桌面");
        }
    }*/

}
