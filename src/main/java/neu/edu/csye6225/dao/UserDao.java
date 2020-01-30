package neu.edu.csye6225.dao;

import neu.edu.csye6225.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zhaocxu
 */
public interface UserDao extends JpaRepository<User, String> {
}
