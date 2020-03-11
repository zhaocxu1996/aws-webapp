package neu.edu.csye6225.dao;

import neu.edu.csye6225.entity.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaDataDao extends JpaRepository<MetaData, String> {
}
