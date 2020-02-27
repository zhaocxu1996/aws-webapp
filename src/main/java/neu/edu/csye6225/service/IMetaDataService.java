package neu.edu.csye6225.service;

import org.springframework.web.multipart.MultipartFile;

public interface IMetaDataService {

    String uploadFile(String billId, MultipartFile file, String fileName, String fileId, String suffix);

    void deleteFile(String objectName);

}
