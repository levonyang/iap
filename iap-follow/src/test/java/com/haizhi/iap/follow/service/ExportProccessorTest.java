package com.haizhi.iap.follow.service;

import com.itextpdf.text.DocumentException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by chenbo on 2017/9/28.
 */
public class ExportProccessorTest {
    ClassPathXmlApplicationContext context;

    @Before
    public void init() {
        context = new ClassPathXmlApplicationContext(
                "classpath*:/spring/applicationContext.xml",
                "classpath*:/spring/applicationContext-data.xml");
    }

    @Test
    public void testPdf() throws IOException, DocumentException {
        PDFExportProcess process = context.getBean(PDFExportProcess.class);
        process.process(1130l);
//        process.process(224l);
    }

//    @Test
//    public void testExcel() {
//        ExcelExportProcess process = context.getBean(ExcelExportProcess.class);
//        process.process(487L);
//    }
}
