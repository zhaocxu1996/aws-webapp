package neu.edu.csye6225.controller;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import neu.edu.csye6225.entity.Bill;
import neu.edu.csye6225.entity.File;
import neu.edu.csye6225.entity.User;
import neu.edu.csye6225.service.IBillService;
import neu.edu.csye6225.service.IFileService;
import neu.edu.csye6225.service.IUserService;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhaocxu
 */
@RestController
public class BillController {

    Logger logger = LoggerFactory.getLogger(BillController.class);
    StatsDClient statsd = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @Autowired
    IUserService iUserService;
    @Autowired
    IBillService iBillService;
    @Autowired
    IFileService iFileService;

    @Value("${topicArn}")
    String TOPIC_ARN;

    @PostMapping(value = "/v1/bill", produces = "application/json")
    public String createBill(HttpServletRequest request, HttpServletResponse response, @RequestBody Bill bill) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("bill.POST");

        if (bill.getVendor() == null || bill.getBill_date() == null ||
                bill.getDue_date() == null || bill.getAmount_due() < 0.01 ||
                bill.getCategories() == null || bill.getPaymentStatus() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.info("Invalid bill content.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.POST-api",stopWatch.getTotalTimeMillis());
            return "Invalid bill content.";
        }
        if (bill.getOwner_id() != null || bill.getId() != null ||
                bill.getCreated_ts() != null || bill.getUpdated_ts() != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.info("Illegal bill content.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.POST-api",stopWatch.getTotalTimeMillis());
            return "Illegal bill content.";
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.POST-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.POST-sql-1",stopWatch.getLastTaskTimeMillis());

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.POST-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        if (bill.getCreated_ts() != null) {
            bill.setCreated_ts(null);
        }
        if (bill.getUpdated_ts() != null) {
            bill.setCreated_ts(null);
        }
        // set the owner id as the user id
        bill.setOwner_id(user.getId());
        Bill returnedBill = iBillService.createBill(bill);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.POST-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
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
        response.setStatus(HttpServletResponse.SC_CREATED);
        logger.info("Bill created.");
        stopWatch.stop();
        statsd.recordExecutionTime("bill.POST-api",stopWatch.getTotalTimeMillis());
        return data.toString();
    }

    @GetMapping(value = "/v1/bills", produces = "application/json")
    public String getAllBills(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("bills.GET");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bills.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("bills.GET-sql-1",stopWatch.getLastTaskTimeMillis());

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bills.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        List<Bill> bills = iBillService.findAllBillsByOwnerId(user.getId());
        stopWatch.stop();
        statsd.recordExecutionTime("bills.GET-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        JsonArray data = new JsonArray();
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
        response.setStatus(HttpServletResponse.SC_OK);
        logger.info("Bills found.");
        stopWatch.stop();
        statsd.recordExecutionTime("bills.GET-api",stopWatch.getTotalTimeMillis());
        return data.toString();
    }

    @GetMapping(value = "/v1/bill/{id}", produces = "application/json")
    public String getBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("bill.GET");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.GET-sql-1",stopWatch.getLastTaskTimeMillis());

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        Bill findBill = iBillService.findById(id);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.GET-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (findBill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("Bill do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.GET-api",stopWatch.getLastTaskTimeMillis());
            return "Bill do not exist.";
        }
        if (!findBill.getOwner_id().equals(user.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Request refused. This bill does not owned by you.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.GET-api",stopWatch.getLastTaskTimeMillis());
            return "Request refused. This bill does not owned by you.";
        }
        JsonObject data = new JsonObject();
        data.addProperty("id", findBill.getId());
        data.addProperty("created_ts", findBill.getCreated_ts());
        data.addProperty("updated_ts", findBill.getUpdated_ts());
        data.addProperty("owner_id", findBill.getOwner_id());
        data.addProperty("vendor", findBill.getVendor());
        data.addProperty("bill_date", findBill.getBill_date());
        data.addProperty("due_date", findBill.getDue_date());
        data.addProperty("amount_due", findBill.getAmount_due());
        JsonArray categories = new JsonArray();
        for (String category : findBill.getCategories()) {
            categories.add(category);
        }
        data.add("categories", categories);
        data.addProperty("paymentStatus", findBill.getPaymentStatus().toString());
        response.setStatus(HttpServletResponse.SC_OK);
        logger.info("Bill found.");
        stopWatch.stop();
        statsd.recordExecutionTime("bills.GET-api",stopWatch.getTotalTimeMillis());
        return data.toString();
    }

    @PutMapping(value = "/v1/bill/{id}", produces = "application/json")
    public String updateBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id, @RequestBody Bill bill) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("bill.PUT");

        if (bill.getId() != null || bill.getCreated_ts() != null ||
                bill.getUpdated_ts() != null || bill.getOwner_id() != null ||
                bill.getAmount_due() < 0.01) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid bill content.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.PUT-api",stopWatch.getTotalTimeMillis());
            return "Invalid bill content.";
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.PUT-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.PUT-sql-1",stopWatch.getLastTaskTimeMillis());

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.PUT-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        Bill findBill = iBillService.findById(id);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.PUT-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (findBill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("Bill do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.PUT-api",stopWatch.getLastTaskTimeMillis());
            return "Bill do not exist.";
        }
        if (!findBill.getOwner_id().equals(user.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Request refused. This bill does not owned by you.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.PUT-api",stopWatch.getLastTaskTimeMillis());
            return "Request refused. This bill does not owned by you.";
        } else {
            stopWatch.stop();
        }

        stopWatch.start("sql");
        Bill updatedBill = iBillService.update(findBill, bill);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.PUT-sql-3",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
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
        response.setStatus(HttpServletResponse.SC_OK);
        logger.info("Bill updated.");
        stopWatch.stop();
        statsd.recordExecutionTime("bill.PUT-api",stopWatch.getTotalTimeMillis());
        return data.toString();
    }

    @DeleteMapping(value = "/v1/bill/{id}", produces = "application/json")
    public String deleteBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("bill.DELETE");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.DELETE-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.DELETE-sql-1",stopWatch.getLastTaskTimeMillis());

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.DELETE-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        Bill findBill = iBillService.findById(id);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.DELETE-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (findBill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("Bill do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.DELETE-api",stopWatch.getLastTaskTimeMillis());
            return "Bill do not exist.";
        }
        if (!findBill.getOwner_id().equals(user.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Request refused. This bill does not owned by you.");
            stopWatch.stop();
            statsd.recordExecutionTime("bill.DELETE-api",stopWatch.getLastTaskTimeMillis());
            return "Request refused. This bill does not owned by you.";
        } else {
            stopWatch.stop();
        }

        stopWatch.start("sql");
        List<File> files = iFileService.findAllByBillId(id);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.DELETE-sql-3",stopWatch.getLastTaskTimeMillis());

        int counter = 4;
        for (File file : files) {
            stopWatch.start("sql");
            iFileService.deleteFile(file);
            stopWatch.stop();
            statsd.recordExecutionTime("bill.DELETE-sql-"+counter,stopWatch.getLastTaskTimeMillis());
            counter++;
        }
        stopWatch.start("sql");
        iBillService.deleteById(id);
        stopWatch.stop();
        statsd.recordExecutionTime("bill.DELETE-sql-"+counter,stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        logger.info("Bill deleted.");
        statsd.recordExecutionTime("bill.DELETE-api",stopWatch.getLastTaskTimeMillis());
        return "Delete bill successfully";
    }

    @GetMapping(value = "/v1/bills/due/{x}", produces = "application/json")
    public String getBillsDue(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "x") String x) throws UnsupportedEncodingException, ParseException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("billsdue.GET");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("billsdue.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        } else if (!isNum(x) || Integer.valueOf(x) < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid path variable.");
            stopWatch.stop();
            statsd.recordExecutionTime("billsdue.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid path variable.";
        }
        stopWatch.stop();
        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("billsdue.GET-sql-1",stopWatch.getLastTaskTimeMillis());

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("billsdue.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        List<Bill> allBills = iBillService.findAllBillsByOwnerId(user.getId());
        stopWatch.stop();
        statsd.recordExecutionTime("billsdue.GET-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        List<Bill> dueBills = new ArrayList<>();
        for (Bill bill : allBills) {
            Date duedate = df.parse(bill.getDue_date());
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(new Date());
            rightNow.add(Calendar.DAY_OF_YEAR,Integer.valueOf(x));
            Date duex = rightNow.getTime();
            if (duedate.before(duex)) {
                dueBills.add(bill);
            }
        }
        AmazonSNS snsClient = AmazonSNSClient.builder().withRegion("us-east-1")
                .withCredentials(new InstanceProfileCredentialsProvider(false)).build();

        StringBuffer message = new StringBuffer();
//        message.append(AWS_REGION);
//        message.append("|");
//        message.append(ROUTE53);
//        message.append("|");
        message.append(user.getEmail_address());
        message.append("|");

        JsonArray jsonArray = new JsonArray();
        for (Bill bill : dueBills) {
            JsonObject jsonObjectRecipe = new JsonObject();
            jsonObjectRecipe.addProperty("id", bill.getId());
            jsonArray.add(jsonObjectRecipe);
            message.append(bill.getId());
            message.append("|");
        }
        snsClient.publish(TOPIC_ARN, message.toString());
        logger.info("sns published");

        response.setStatus(HttpServletResponse.SC_OK);
        logger.info("bills due in " + x + " days got");
        stopWatch.stop();
        statsd.recordExecutionTime("billsdue.GET-api", stopWatch.getTotalTimeMillis());
        return jsonArray.toString();
    }

    private boolean isNum(String x) {
        for (char c : x.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
