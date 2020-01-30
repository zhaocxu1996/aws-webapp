package neu.edu.csye6225;

import neu.edu.csye6225.controller.BillController;
import neu.edu.csye6225.entity.Bill;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

public class BillTest {

    @Test
    void createBillTest() throws UnsupportedEncodingException {
        Bill bill = new Bill();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BillController billController = new BillController();
        Assert.assertTrue("Invalid bill content.".equals(billController.createBill(request, response, bill)));
    }
}
