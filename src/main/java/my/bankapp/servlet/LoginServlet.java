package my.bankapp.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.service.UserService;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private ServiceFactory serviceFactory;
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
        logger = serviceFactory.getLogger();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.getRequestDispatcher("/login.jsp").forward(req, resp);
        logger.info("doGet = " + req);
    }
}
