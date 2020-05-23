package com.haizhi.iap.search.repo.atlas;

import com.haizhi.iap.search.model.DcStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DcStoreRepoTest {

    @Autowired
    private DcStoreRepo dcStoreRepo;

    @Test
    public void findDcStore() {
        DcStore gdb = dcStoreRepo.findDcStore("GDB");
        System.out.println(gdb);
    }
}