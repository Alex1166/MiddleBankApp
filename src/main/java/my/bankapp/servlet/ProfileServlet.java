package my.bankapp.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.bankapp.dto.AccountDto;
import my.bankapp.dto.UserDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.Account;
import my.bankapp.model.User;
import my.bankapp.service.AccountService;
import my.bankapp.service.UserService;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {

    private ServiceFactory serviceFactory;
    private UserService userService;
    private AccountService accountService;
    private Logger logger;

    @Override
    public void init() throws ServletException {
        System.out.println("ProfileServlet init");
        Object object = getServletContext().getAttribute("serviceFactory");
        if (object instanceof ServiceFactory) {
            serviceFactory = (ServiceFactory) object;
        } else {
            throw new IllegalStateException();
        }

        userService = serviceFactory.getUserService();
        accountService = serviceFactory.getAccountService();
        logger = serviceFactory.getLogger();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("ProfileServlet doGet");
        long currentUserId = (long) (req.getSession().getAttribute("userId"));

        UserDto user = userService.getUserById(currentUserId);
        logger.info("currentUserId = " + currentUserId);

        List<AccountDto> accountList = accountService.getAccountList(currentUserId);
        logger.info("accountList = " + accountList);
        logger.info("user = " + user);

        req.setAttribute("user", user);
        req.setAttribute("accountList", accountList);

        logger.info("profileGet = " + req);
        req.getRequestDispatcher("/profile.jsp").forward(req, resp);
    }
}
