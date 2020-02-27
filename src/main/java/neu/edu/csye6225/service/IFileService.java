package neu.edu.csye6225.service;

import neu.edu.csye6225.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileService {

    void attachFile(String billId, MultipartFile file, String suffix);

    File findByFilename(String fileName);

    File findById(String id);

    void deleteFile(File file);

    List<File> findAllByBillId(String billId);
}
