package neu.edu.csye6225.service.impl;

import neu.edu.csye6225.dao.FileDao;
import neu.edu.csye6225.entity.File;
import neu.edu.csye6225.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class IFileServiceImpl implements IFileService {

    @Autowired
    FileDao fileDao;

    @Override
    public void attachFile(String billId, MultipartFile file) {
        String file_name = file.getOriginalFilename();
        String filePath = "/home/zhaocxu/tmp/";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String upload_date = df.format(new Date());
        String url =  filePath + billId + "_" + file_name;
        File target = new File();
        target.setFile_name(file_name);
        target.setUrl(url);
        target.setUpload_date(upload_date);
        fileDao.save(target);
        java.io.File localFile = new java.io.File(url);
        if (!localFile.getParentFile().exists()) {
            localFile.getParentFile().mkdir();
        }
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File findByFilename(String fileName) {
        List<File> files = fileDao.findAll();
        for (File file : files) {
            if (file.getFile_name().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    @Override
    public File findById(String id) {
        Optional<File> optionalFile = fileDao.findById(id);
        if (optionalFile.isPresent()) {
            return optionalFile.get();
        } else {
            return null;
        }
    }

    @Override
    public void deleteFile(File file) {
        java.io.File localFile = new java.io.File(file.getUrl());
        localFile.delete();
        fileDao.delete(file);
    }

    @Override
    public List<File> findAllByBillId(String billId) {
        List<File> files = fileDao.findAll();
        List<File> targetList = new ArrayList<>();
        String filePath = "/home/zhaocxu/tmp/";
        for (File file : files) {
            java.io.File localFile = new java.io.File(file.getUrl());
            String uniqueFileName = localFile.getName();
            int position = uniqueFileName.indexOf("_");
            String targetBillId = uniqueFileName.substring(0, position);
            if (targetBillId.equals(billId)) {
                targetList.add(file);
            }
        }
        return targetList;
    }
}
