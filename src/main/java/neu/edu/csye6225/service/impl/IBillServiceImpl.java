package neu.edu.csye6225.service.impl;

import neu.edu.csye6225.dao.BillDao;
import neu.edu.csye6225.entity.Bill;
import neu.edu.csye6225.service.IBillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author zhaocxu
 */
@Service
public class IBillServiceImpl implements IBillService {

    Logger logger = LoggerFactory.getLogger(IBillServiceImpl.class);

    @Autowired
    BillDao billDao;

    @Override
    public Bill createBill(Bill bill) {
        // set create and update time
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        bill.setCreated_ts(date);
        bill.setUpdated_ts(date);

        billDao.save(bill);
        logger.info("bill saved.");
        return bill;
    }

    @Override
    public List<Bill> findAllBillsByOwnerId(String ownerId) {
        List<Bill> allBills = billDao.findAll();
        List<Bill> targetBills = new ArrayList<>();
        for (Bill bill : allBills) {
            if (bill.getOwner_id().equals(ownerId)) {
                targetBills.add(bill);
            }
        }
        return targetBills;
    }

    @Override
    public Bill findById(String id) {
        Optional<Bill> billOptional = billDao.findById(id);
        if (billOptional.isPresent()) {
            logger.info("bill found.");
            return billOptional.get();
        } else {
            logger.info("bill not exist");
            return null;
        }
    }

    @Override
    public Bill update(Bill origin, Bill update) {
        if (update.getVendor() != null) {
            origin.setVendor(update.getVendor());
        }
        if (update.getBill_date() != null) {
            origin.setBill_date(update.getBill_date());
        }
        if (update.getDue_date() != null) {
            origin.setDue_date(update.getDue_date());
        }
        if (update.getAmount_due() >= 0.01) {
            origin.setAmount_due(update.getAmount_due());
        }
        if (update.getCategories() != null) {
            origin.setCategories(update.getCategories());
        }
        if (update.getPaymentStatus() != null) {
            origin.setPaymentStatus(update.getPaymentStatus());
        }
        // change update time
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        origin.setUpdated_ts(date);
        billDao.save(origin);
        logger.info("bill updated.");
        return origin;
    }

    @Override
    public void deleteById(String id) {
        billDao.deleteById(id);
        logger.info("bill deleted.");
    }
}
