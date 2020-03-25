package neu.edu.csye6225.service.impl;

import neu.edu.csye6225.dao.FileDao;
import neu.edu.csye6225.entity.File;
import neu.edu.csye6225.service.IFileService;
import neu.edu.csye6225.service.IMetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class IFileServiceImpl implements IFileService {

    Logger logger = LoggerFactory.getLogger(IFileServiceImpl.class);

    @Autowired
    FileDao fileDao;
    @Autowired
    IMetaDataService iMetaDataService;

    @Override
    public void attachFile(String billId, MultipartFile file, String suffix) {
        String id = UUID.randomUUID().toString();
        String file_name = file.getOriginalFilename();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String upload_date = df.format(new Date());
//        String filePath = "/home";
//        String url =  filePath + billId + "_" + file_name;
        String url = iMetaDataService.uploadFile(billId, file, file_name, id, suffix);
//        long size = file.getSize();
//        String md5 = "";
//        try {
//            md5 = new String(Base64.encodeBase64(DigestUtils.md5(file.getBytes())));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        File target = new File();
        target.setId(id);
        target.setFile_name(file_name);
        target.setUrl(url);
        target.setUpload_date(upload_date);
//        target.setSize(size);
//        target.setMd5(md5);
//        target.setType(suffix);
        fileDao.save(target);
        logger.info("file saved.");
//        java.io.File localFile = new java.io.File(url);
//        if (!localFile.getParentFile().exists()) {
//            localFile.getParentFile().mkdir();
//        }
//        try {
//            file.transferTo(localFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public File findByFilename(String fileName) {
        List<File> files = fileDao.findAll();
        for (File file : files) {
            if (file.getFile_name().equals(fileName)) {
                logger.info("file found.");
                return file;
            }
        }
        logger.info("fill not found.");
        return null;
    }

    @Override
    public File findById(String id) {
        Optional<File> optionalFile = fileDao.findById(id);
        if (optionalFile.isPresent()) {
            logger.info("file found.");
            return optionalFile.get();
        } else {
            logger.info("fill not found.");
            return null;
        }
    }

    @Override
    public void deleteFile(File file) {
//        java.io.File localFile = new java.io.File(file.getUrl());
//        localFile.delete();
        iMetaDataService.deleteFile(file.getFile_name());
        logger.info("file deleted.");
        fileDao.delete(file);
    }

    @Override
    public List<File> findAllByBillId(String billId) {
        List<File> files = fileDao.findAll();
        List<File> targetList = new ArrayList<>();
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
