package neu.edu.csye6225.dao;

import neu.edu.csye6225.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zhaocxu
 */
public interface BillDao extends JpaRepository<Bill, String> {
}
