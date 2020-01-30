package neu.edu.csye6225.service;

import neu.edu.csye6225.entity.Bill;

import java.util.List;

/**
 * @author zhaocxu
 */
public interface IBillService {

    Bill createBill(Bill bill);

    List<Bill> findAllBillsByOwnerId(String ownerId);

    Bill findById(String id);

    Bill update(Bill origin, Bill update);

    void deleteById(String id);
}
