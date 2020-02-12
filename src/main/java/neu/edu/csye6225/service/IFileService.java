package neu.edu.csye6225.service;

import neu.edu.csye6225.entity.File;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    void attachFile(String billId, MultipartFile file);

    File findByFilename(String fileName);

    File findById(String id);

    void deleteFile(File file);
}
