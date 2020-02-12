package neu.edu.csye6225.service.impl;

import neu.edu.csye6225.entity.File;
import neu.edu.csye6225.service.IFileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IFileServiceImplTest {

    @Autowired
    IFileService iFileService;

    @Test
    public void findByFilename() {
        String file_path = "/home/zhaocxu/tmp/";
        String billId = "ff8081817017be27017018200a4a0004";
        String file_name = "hawk.jpeg";
        File file = iFileService.findByFilename(file_name);
        Assert.assertEquals(file.getUrl(), file_path+billId+"_"+file_name);
    }

    @Test
    public void findById() {
        String file_id = "ff80818170395b0601703b71811a0001";
        File file = iFileService.findById(file_id);
        Assert.assertEquals(file.getFile_name(), "hawk.jpeg");
    }

    @Test
    @Transactional
    public void deleteFile() {
        String file_id = "ff80818170395b0601703b71811a0001";
        File file = iFileService.findById(file_id);
        iFileService.deleteFile(file);
        java.io.File localFile = new java.io.File(file.getUrl());
        Assert.assertFalse(localFile.exists());
    }
}