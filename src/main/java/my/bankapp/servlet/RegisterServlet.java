package my.bankapp.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.User;
import my.bankapp.service.AccountService;
import my.bankapp.service.UserService;

import java.io.IOException;
import java.util.Arrays;

@WebServlet(urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    private ServiceFactory serviceFactory;
    private UserService userService;

    @Override
    public void init() throws ServletException {
        System.out.println("RegisterServlet init");
        Object object = getServletContext().getAttribute("serviceFactory");
        if (object instanceof ServiceFactory) {
            serviceFactory = (ServiceFactory) object;
        } else {
            throw new IllegalStateException();
        }

        userService = serviceFactory.getUserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.getRequestDispatcher("/register.jsp").forward(req, resp);
        System.out.println("doGet = " + req);
    }
}
