package my.bankapp.servlet.api.v1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.bankapp.dto.AccountDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.Transaction;
import my.bankapp.service.AccountService;
import my.bankapp.service.TransactionService;
import my.bankapp.service.UserService;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@WebServlet(urlPatterns = "/api/v1/deposit")
public class DepositApi extends ApiHttpServlet {

    private ServiceFactory serviceFactory;
    private TransactionService transactionService;
    private AccountService accountService;
    private UserService userService;
    private Logger logger;

    @Override
    public void init() throws ServletException {
        System.out.println("LoginServlet init");
        Object object = getServletContext().getAttribute("serviceFactory");
        if (object instanceof ServiceFactory) {
            serviceFactory = (ServiceFactory) object;
        } else {
            throw new IllegalStateException();
        }

        userService = serviceFactory.getUserService();
        accountService = serviceFactory.getAccountService();
        transactionService = serviceFactory.getTransactionService();
        logger = serviceFactory.getLogger();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        logger.info("doPost");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(req.getInputStream(), new TypeReference<>() {
        });

        System.out.println("account: " + data.get("account"));
        System.out.println("money: " + data.get("money"));

        long accountId = Long.parseLong((String) data.get("account"));
        String moneyValue = (String) data.get("money");
        try {
            AccountDto accountSender = accountService.getCashAccount();
            AccountDto accountRecipient = accountService.getAccountById(accountId);
            BigDecimal money = new BigDecimal(moneyValue);

            Transaction transaction = transactionService.transferMoney(accountSender.getId(), accountRecipient.getId(), money);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (RuntimeException e) {
            resp.setHeader("X-Status-Message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
