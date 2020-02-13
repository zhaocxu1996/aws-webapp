package neu.edu.csye6225.dao;

import neu.edu.csye6225.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDao extends JpaRepository<File, String> {
}
