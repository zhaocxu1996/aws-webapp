package neu.edu.csye6225.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import neu.edu.csye6225.dao.MetaDataDao;
import neu.edu.csye6225.entity.MetaData;
import neu.edu.csye6225.service.IMetaDataService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Service
public class IMetaDataServiceImpl implements IMetaDataService {

    @Value("${aws_access_key}")
    String awsAccessKey;
    @Value("${aws_secret_key}")
    String awsSecretKey;
    @Value("${bucketName}")
    String bucketName;
    @Value("${region}")
    String region;

    public static String LOCAL_DIR = "/tmp/";

    @Autowired
    MetaDataDao metaDataDao;

    @Override
    public String uploadFile(String billId, MultipartFile file, String fileName, String fileId, String suffix) {
        MetaData metaData = new MetaData();
        metaData.setFile_id(fileId);
        metaData.setType(suffix);
        String localFilePath = LOCAL_DIR + billId + "_" + fileName;
        java.io.File localFile = new File(localFilePath);
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long size = file.getSize();
        metaData.setSize(size);
        String md5 = "";
        try {
            md5 = new String(Base64.encodeBase64(DigestUtils.md5(file.getBytes())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        metaData.setMd5(md5);
        metaDataDao.save(metaData);

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(region)
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
                    .withPathStyleAccessEnabled(true).build();
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, localFile);
            s3Client.putObject(request);
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, fileName);
            URL url = s3Client.generatePresignedUrl(urlRequest);
            localFile.delete();
            return url.toString();
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deleteFile(String objectName) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
                .build();
        try {
            s3Client.deleteObject(bucketName, objectName);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        }
    }
}
