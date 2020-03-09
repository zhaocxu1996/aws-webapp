package neu.edu.csye6225.controller;

import com.google.gson.JsonObject;
import neu.edu.csye6225.entity.Bill;
import neu.edu.csye6225.entity.File;
import neu.edu.csye6225.entity.User;
import neu.edu.csye6225.service.IBillService;
import neu.edu.csye6225.service.IFileService;
import neu.edu.csye6225.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

@RestController
public class FileController {

    @Autowired
    IUserService iUserService;
    @Autowired
    IBillService iBillService;
    @Autowired
    IFileService iFileService;

    @PostMapping(value = "/v1/bill/{id}/file", produces = "application/json")
    public String attachFile(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "id") String id, @RequestParam(value = "file") MultipartFile file) throws UnsupportedEncodingException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Invalid file.";
        }
        Bill bill = iBillService.findById(id);
        if (bill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Bill do not exist.";
        }
        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        if (!bill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Request refused. This bill does not owned by you.";
        }
        // find if the suffix of file legal
        String fileName = file.getOriginalFilename();
        if (iFileService.findByFilename(fileName) != null) {
            File exist = iFileService.findByFilename(fileName);
            java.io.File localFile = new java.io.File(exist.getUrl());
            String uniqueFileName = localFile.getName();
            int position = uniqueFileName.indexOf("_");
            String targetBillId = uniqueFileName.substring(0, position);
            if (targetBillId.equals(bill.getId())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "File already exist.";
            }
        }
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        String requirement = "pdf,png,jpg,jpeg";
        if(requirement.indexOf(suffix) == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Illegal file type.";
        }
        String filePath = "/home";

        iFileService.attachFile(bill.getId(), file);

        File target = iFileService.findByFilename(fileName);
        if (target != null) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            JsonObject result = new JsonObject();
            result.addProperty("file_name", target.getFile_name());
            result.addProperty("id", target.getId());
            result.addProperty("url", target.getUrl());
            result.addProperty("upload_date", target.getUpload_date());
            return result.toString();
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "File do not exist.";
        }
    }

    @GetMapping(value = "/v1/bill/{billId}/file/{fileId}", produces = "application/json")
    public String getBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "billId") String billId, @PathVariable(name = "fileId") String fileId) throws UnsupportedEncodingException {
        Bill bill = iBillService.findById(billId);
        if (bill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Bill do not exist.";
        }
        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        if (!bill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Request refused. This bill does not owned by you.";
        }

        File file = iFileService.findById(fileId);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "File do not exist.";
        }
        java.io.File localFile = new java.io.File(file.getUrl());
        String uniqueFileName = localFile.getName();
        int position = uniqueFileName.indexOf("_");
        String targetBillId = uniqueFileName.substring(0, position);
        System.out.println(targetBillId);
        if (targetBillId.equals(billId)) {
            response.setStatus(HttpServletResponse.SC_OK);
            JsonObject result = new JsonObject();
            result.addProperty("file_name", file.getFile_name());
            result.addProperty("id", file.getId());
            result.addProperty("url", file.getUrl());
            result.addProperty("upload_date", file.getUpload_date());
            return result.toString();
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "File id do not match bill id.";
        }
    }

    @DeleteMapping(value = "/v1/bill/{billId}/file/{fileId}", produces = "application/json")
    public String deleteBill(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "billId") String billId, @PathVariable(name = "fileId") String fileId) throws UnsupportedEncodingException {
        Bill bill = iBillService.findById(billId);
        if (bill == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Bill do not exist.";
        }
        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        User tokenUser = iUserService.findUserByEmail(username);

        if (!bill.getOwner_id().equals(tokenUser.getId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "Request refused. This bill does not owned by you.";
        }

        File file = iFileService.findById(fileId);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "File do not exist.";
        }
        java.io.File localFile = new java.io.File(file.getUrl());
        String uniqueFileName = localFile.getName();
        int position = uniqueFileName.indexOf("_");
        String targetBillId = uniqueFileName.substring(0, position);
        System.out.println(targetBillId);
        if (targetBillId.equals(billId)) {
            iFileService.deleteFile(file);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return "File delete success.";
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "File id do not match bill id.";
        }
    }
}
