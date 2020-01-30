package neu.edu.csye6225.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import neu.edu.csye6225.entity.Bill;
import neu.edu.csye6225.entity.User;
import neu.edu.csye6225.service.IBillService;
import neu.edu.csye6225.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

/**
 * @author zhaocxu
 */
@RestController
public class BillController {

    @Autowired
    IUserService iUserService;
    @Autowired
    IBillService iBillService;

    @PostMapping(value = "/v1/bill", produces = "application/json")
    public String createBill(HttpServletRequest request, HttpServletResponse response, @RequestBody Bill bill) throws UnsupportedEncodingException {

        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        if (bill.getVendor() == null || bill.getBill_date() == null ||
                bill.getDue_date() == null || bill.getAmount_due() < 0.01 ||
                bill.getCategories() == null || bill.getPaymentStatus() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Invalid bill content.";
        }
        if (bill.getOwner_id() != null || bill.getId() != null ||
                bill.getCreated_ts() != null || bill.getUpdated_ts() != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Illegal bill content.";
        }
        if (bill.getCreated_ts() != null) {
            bill.setCreated_ts(null);
        }
        if (bill.getUpdated_ts() != null) {
            bill.setCreated_ts(null);
        }
        // set the owner id as the user id
        bill.setOwner_id(tokenUser.getId());
        Bill returnedBill = iBillService.createBill(bill);
        response.setStatus(HttpServletResponse.SC_CREATED);
        JsonObject data = new JsonObject();
        data.addProperty("id", returnedBill.getId());
        data.addProperty("created_ts", returnedBill.getCreated_ts());
        data.addProperty("updated_ts", returnedBill.getUpdated_ts());
        data.addProperty("owner_id", returnedBill.getOwner_id());
        data.addProperty("vendor", returnedBill.getVendor());
        data.addProperty("bill_date", returnedBill.getBill_date());
        data.addProperty("due_date", returnedBill.getDue_date());
        data.addProperty("amount_due", returnedBill.getAmount_due());
        JsonArray categories = new JsonArray();
        for (String category : bill.getCategories()) {
            categories.add(category);
        }
        data.add("categories", categories);
        data.addProperty("paymentStatus", returnedBill.getPaymentStatus().toString());
        return data.toString();
    }

    @GetMapping(value = "/v1/bills", produces = "application/json")
    public String getAllBills(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        JsonArray data = new JsonArray();
        response.setStatus(HttpServletResponse.SC_OK);
        List<Bill> bills = iBillService.findAllBillsByOwnerId(tokenUser.getId());
        for (Bill bill : bills) {
            JsonObject billJsonObject = new JsonObject();
            billJsonObject.addProperty("id", bill.getId());
            billJsonObject.addProperty("created_ts", bill.getCreated_ts());
            billJsonObject.addProperty("updated_ts", bill.getUpdated_ts());
            billJsonObject.addProperty("owner_id", bill.getOwner_id());
            billJsonObject.addProperty("vendor", bill.getVendor());
            billJsonObject.addProperty("bill_date", bill.getBill_date());
            billJsonObject.addProperty("due_date", bill.getDue_date());
            billJsonObject.addProperty("amount_due", bill.getAmount_due());
            JsonArray categories = new JsonArray();
            for (String category : bill.getCategories()) {
                categories.add(category);
            }
            billJsonObject.add("categories", categories);
            billJsonObject.addProperty("paymentStatus", bill.getPaymentStatus().toString());
            data.add(billJsonObject);
        }
        return data.toString();
    }

    @GetMapping(value = "/v1/bill/{id}", produces = "application/json")
    public String getBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id) throws UnsupportedEncodingException {

        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        Bill bill = iBillService.findById(id);
        if (bill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Bill do not exist.";
        }
        if (!bill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Request refused. This bill does not owned by you.";
        }
        response.setStatus(HttpServletResponse.SC_OK);
        JsonObject data = new JsonObject();
        data.addProperty("id", bill.getId());
        data.addProperty("created_ts", bill.getCreated_ts());
        data.addProperty("updated_ts", bill.getUpdated_ts());
        data.addProperty("owner_id", bill.getOwner_id());
        data.addProperty("vendor", bill.getVendor());
        data.addProperty("bill_date", bill.getBill_date());
        data.addProperty("due_date", bill.getDue_date());
        data.addProperty("amount_due", bill.getAmount_due());
        JsonArray categories = new JsonArray();
        for (String category : bill.getCategories()) {
            categories.add(category);
        }
        data.add("categories", categories);
        data.addProperty("paymentStatus", bill.getPaymentStatus().toString());
        return data.toString();
    }

    @PutMapping(value = "/v1/bill/{id}", produces = "application/json")
    public String updateBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id, @RequestBody Bill bill) throws UnsupportedEncodingException {
        if (bill.getId() != null || bill.getCreated_ts() != null ||
                bill.getUpdated_ts() != null || bill.getOwner_id() != null ||
                bill.getAmount_due() < 0.01) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Invalid bill content.";
        }

        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        Bill findBill = iBillService.findById(id);
        if (findBill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Bill do not exist.";
        }
        if (!findBill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Request refused. This bill does not owned by you.";
        }

        Bill updatedBill = iBillService.update(findBill, bill);
        response.setStatus(HttpServletResponse.SC_OK);
        JsonObject data = new JsonObject();
        data.addProperty("id", updatedBill.getId());
        data.addProperty("created_ts", updatedBill.getCreated_ts());
        data.addProperty("updated_ts", updatedBill.getUpdated_ts());
        data.addProperty("owner_id", updatedBill.getOwner_id());
        data.addProperty("vendor", updatedBill.getVendor());
        data.addProperty("bill_date", updatedBill.getBill_date());
        data.addProperty("due_date", updatedBill.getDue_date());
        data.addProperty("amount_due", updatedBill.getAmount_due());
        JsonArray categories = new JsonArray();
        for (String category : updatedBill.getCategories()) {
            categories.add(category);
        }
        data.add("categories", categories);
        data.addProperty("paymentStatus", updatedBill.getPaymentStatus().toString());
        return data.toString();
    }

    @DeleteMapping(value = "/v1/bill/{id}", produces = "application/json")
    public String deleteBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id) throws UnsupportedEncodingException {
        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        Bill findBill = iBillService.findById(id);
        if (findBill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Bill do not exist.";
        }
        if (!findBill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Request refused. This bill does not owned by you.";
        }

        iBillService.deleteById(id);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return "Delete bill successfully";
    }
}
