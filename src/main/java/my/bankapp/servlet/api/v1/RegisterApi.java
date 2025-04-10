package my.bankapp.servlet.api.v1;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.bankapp.dto.UserDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.User;
import my.bankapp.service.UserService;

import java.io.IOException;
import java.util.Arrays;

@WebServlet(urlPatterns = "/api/v1/register")
public class RegisterApi extends ApiHttpServlet {

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("doPost = " + req.getMethod());

//        System.out.println("query string: " + req.getQueryString());
//        System.out.println("body: " + req.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
//
//        BufferedReader reader = req.getReader();
//        String requestBody = reader.lines().collect(Collectors.joining());
//        System.out.println("Raw Request Body: " + requestBody);

        String login = req.getParameter("username");
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        System.out.println("getParameterMap: ");
        req.getParameterMap().forEach((key, value) -> System.out.println(key + ":" + Arrays.toString(value)));
        System.out.println("login = " + login);
        System.out.println("name = " + name);
        System.out.println("password = " + password);

        HttpSession session = req.getSession(true);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "СЕССИЯ НЕ НАЙДЕНА");
            return;
        }

        try {
            UserDto user = userService.createNewUser(login, name, password);

            System.out.println("Session ID: " + session.getId());
            session.setAttribute("userId", user.getId());
            System.out.println("req.getServletPath() = " + req.getServletPath());
            System.out.println("req.getPathInfo() = " + req.getPathInfo());
            System.out.println("req.getContextPath() = " + req.getContextPath());
            System.out.println(
                    "request.getServletContext().getAttribute(\"loginRedirect\") = " + req.getServletContext().getAttribute("loginRedirect"));
//                resp.sendRedirect(req.getServletPath());
//            resp.sendRedirect(req.getContextPath() + req.getServletContext().getAttribute("loginRedirect"));
            req.getServletContext().setAttribute("loginRedirect", "/");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (RuntimeException e) {
            session.setAttribute("userId", null);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }
}
