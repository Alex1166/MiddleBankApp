package my.bankapp.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.service.UserService;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Enumeration;

@WebServlet(urlPatterns = "/error")
public class ErrorServlet extends HttpServlet {

    private ServiceFactory serviceFactory;
    private UserService userService;
    private Logger logger;

    @Override
    public void init() {
        System.out.println("ErrorServlet init");
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try {
            Enumeration<String> attributeNames = req.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attr = attributeNames.nextElement();
                System.out.println(attr + " = " + req.getAttribute(attr));
            }

            System.out.println("RequestDispatcher.ERROR_EXCEPTION_TYPE = " + RequestDispatcher.ERROR_EXCEPTION_TYPE);
            System.out.println("RequestDispatcher.ERROR_EXCEPTION = " + RequestDispatcher.ERROR_EXCEPTION);

            Object excType = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE);

            System.out.println("excType = " + excType);
            System.out.println("excType = " + excType instanceof String);

            if ("java.lang.Exception".equals(excType)) {
                Exception exception = (Exception) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                System.out.println("exception = " + exception);
            }

            Throwable throwable = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

            logger.error(throwable);
            logger.error(throwable.getMessage());
//            resp.setHeader("X-Status-Message", "444444444455555");
        resp.setHeader("X-Status-Message", throwable.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, throwable.getMessage());
            } catch (Exception e) {
                logger.error("Error while handling error");
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
