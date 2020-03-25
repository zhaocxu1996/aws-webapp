package neu.edu.csye6225.controller;

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
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

@RestController
public class FileController {

    Logger logger = LoggerFactory.getLogger(FileController.class);
    StatsDClient statsd = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @Autowired
    IUserService iUserService;
    @Autowired
    IBillService iBillService;
    @Autowired
    IFileService iFileService;

    @PostMapping(value = "/v1/bill/{id}/file", produces = "application/json")
    public String attachFile(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id, @RequestParam(value = "file") MultipartFile file) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("file.POST");

        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid file.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
            return "Invalid file.";
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
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
        statsd.recordExecutionTime("file.POST-sql-1",stopWatch.getLastTaskTimeMillis());

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        Bill bill = iBillService.findById(id);
        stopWatch.stop();
        statsd.recordExecutionTime("file.POST-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (bill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("Bill do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
            return "Bill do not exist.";
        }

        if (!bill.getOwner_id().equals(user.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Request refused. This bill does not owned by you.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
            return "Request refused. This bill does not owned by you.";
        }
        // find if the suffix of file legal
        String fileName = file.getOriginalFilename();
//        if (iFileService.findByFilename(fileName) != null) {
//            File exist = iFileService.findByFilename(fileName);
//            java.io.File localFile = new java.io.File(exist.getUrl());
//            String uniqueFileName = localFile.getName();
//            int position = uniqueFileName.indexOf("_");
//            String targetBillId = uniqueFileName.substring(0, position);
//            if (targetBillId.equals(bill.getId())) {
//                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                return "File already exist.";
//            }
//        }
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        String requirement = "pdf,png,jpg,jpeg";
        if(requirement.indexOf(suffix) == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Illegal file type.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
            return "Illegal file type.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        iFileService.attachFile(bill.getId(), file, suffix);
        stopWatch.stop();
        statsd.recordExecutionTime("file.POST-sql-3",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("sql");
        File target = iFileService.findByFilename(fileName);
        stopWatch.stop();
        statsd.recordExecutionTime("file.POST-sql-4",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (target != null) {
            JsonObject result = new JsonObject();
            result.addProperty("file_name", target.getFile_name());
            result.addProperty("id", target.getId());
            result.addProperty("url", target.getUrl());
            result.addProperty("upload_date", target.getUpload_date());
            response.setStatus(HttpServletResponse.SC_CREATED);
            logger.info("File attached.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
            return result.toString();
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("File do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.POST-api",stopWatch.getTotalTimeMillis());
            return "File do not exist.";
        }
    }

    @GetMapping(value = "/v1/bill/{billId}/file/{fileId}", produces = "application/json")
    public String getFile(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "billId") String billId, @PathVariable(name = "fileId") String fileId) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("file.GET");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.GET-api",stopWatch.getTotalTimeMillis());
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
        User tokenUser = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("file.GET-sql-1",stopWatch.getLastTaskTimeMillis());

        if (tokenUser == null || !BCrypt.checkpw(password, tokenUser.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        Bill bill = iBillService.findById(billId);
        stopWatch.stop();
        statsd.recordExecutionTime("file.GET-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (bill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("Bill do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.GET-api",stopWatch.getTotalTimeMillis());
            return "Bill do not exist.";
        }

        if (!bill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Request refused. This bill does not owned by you.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.GET-api",stopWatch.getTotalTimeMillis());
            return "Request refused. This bill does not owned by you.";
        } else {
            stopWatch.stop();
        }

        stopWatch.start("sql");
        File file = iFileService.findById(fileId);
        stopWatch.stop();
        statsd.recordExecutionTime("file.GET-sql-3",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("File do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.GET-api",stopWatch.getTotalTimeMillis());
            return "File do not exist.";
        }
        java.io.File localFile = new java.io.File(file.getUrl());
        String uniqueFileName = localFile.getName();
        int position = uniqueFileName.indexOf("_");
        String targetBillId = uniqueFileName.substring(0, position);
        System.out.println(targetBillId);
        if (targetBillId.equals(billId)) {
            JsonObject result = new JsonObject();
            result.addProperty("file_name", file.getFile_name());
            result.addProperty("id", file.getId());
            result.addProperty("url", file.getUrl());
            result.addProperty("upload_date", file.getUpload_date());
            response.setStatus(HttpServletResponse.SC_OK);
            logger.info("File found.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.GET-api",stopWatch.getTotalTimeMillis());
            return result.toString();
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("File id do not match bill id.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.GET-api",stopWatch.getTotalTimeMillis());
            return "File id do not match bill id.";
        }
    }

    @DeleteMapping(value = "/v1/bill/{billId}/file/{fileId}", produces = "application/json")
    public String deleteFile(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "billId") String billId, @PathVariable(name = "fileId") String fileId) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("file.DELETE");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-api",stopWatch.getTotalTimeMillis());
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
        User tokenUser = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("file.DELETE-sql-1",stopWatch.getLastTaskTimeMillis());

        if (tokenUser == null || !BCrypt.checkpw(password, tokenUser.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        Bill bill = iBillService.findById(billId);
        stopWatch.stop();
        statsd.recordExecutionTime("file.DELETE-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (bill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("Bill do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-api",stopWatch.getTotalTimeMillis());
            return "Bill do not exist.";
        }

        if (!bill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Request refused. This bill does not owned by you.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-api",stopWatch.getTotalTimeMillis());
            return "Request refused. This bill does not owned by you.";
        } else {
            stopWatch.stop();
        }

        stopWatch.start("sql");
        File file = iFileService.findById(fileId);
        stopWatch.stop();
        statsd.recordExecutionTime("file.DELETE-sql-3",stopWatch.getLastTaskTimeMillis());
        if (file == null) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("File do not exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-api",stopWatch.getTotalTimeMillis());
            return "File do not exist.";
        }
        java.io.File localFile = new java.io.File(file.getUrl());
        String uniqueFileName = localFile.getName();
        int position = uniqueFileName.indexOf("_");
        String targetBillId = uniqueFileName.substring(0, position);
        if (targetBillId.equals(billId)) {
            stopWatch.start("sql");
            iFileService.deleteFile(file);
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-sql-4",stopWatch.getLastTaskTimeMillis());

            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            logger.info("File deleted.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-api",stopWatch.getTotalTimeMillis());
            return "File delete success.";
        } else {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            logger.error("File id do not match bill id.");
            stopWatch.stop();
            statsd.recordExecutionTime("file.DELETE-api",stopWatch.getTotalTimeMillis());
            return "File id do not match bill id.";
        }
    }
}
