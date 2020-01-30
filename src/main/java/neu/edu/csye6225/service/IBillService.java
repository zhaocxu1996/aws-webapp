package neu.edu.csye6225.service;

import neu.edu.csye6225.entity.Bill;

import java.util.List;

/**
 * @author zhaocxu
 */
public interface IBillService {

    /**
     * Create a bill into database
     * @param bill
     * @return a bill with create&update time
     */
    Bill createBill(Bill bill);

    /**
     * Find all bills of current user
     * @param ownerId
     * @return list of these bills
     */
    List<Bill> findAllBillsByOwnerId(String ownerId);

    /**
     * Get the bill by the bill id
     * @param id
     * @return target bill
     */
    Bill findById(String id);

    /**
     * Update a bill
     * @param origin origin bill
     * @param update updated bill which need to replace old one
     * @return updated bill
     */
    Bill update(Bill origin, Bill update);

    /**
     * Delete a bill by bill id
     * @param id
     */
    void deleteById(String id);
}
