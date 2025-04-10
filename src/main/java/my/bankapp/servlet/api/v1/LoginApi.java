package my.bankapp.servlet.api.v1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.bankapp.dto.AccountDto;
import my.bankapp.dto.UserDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.User;
import my.bankapp.service.UserService;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(urlPatterns = "/api/v1/login")
public class LoginApi extends ApiHttpServlet {

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
        long currentUserId = (long) req.getSession().getAttribute("userId");

        UserDto userDto;

        try {
            userDto = userService.getUserById(currentUserId);
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String accountJson = objectMapper.writeValueAsString(userDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");

        PrintWriter writer = resp.getWriter();
        writer.print(accountJson);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

//        System.out.println("doPost = " + req.getMethod());
        logger.debug("doPost = " + req.getMethod());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(req.getInputStream(), new TypeReference<>() {});

        logger.debug("data = " + data);

        String username = (String) data.get("username");
        String password = (String) data.get("password");

//        System.out.println("query string: " + req.getQueryString());
//        System.out.println("body: " + req.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
//
//        BufferedReader reader = req.getReader();
//        String requestBody = reader.lines().collect(Collectors.joining());
//        System.out.println("Raw Request Body: " + requestBody);

//        String username = req.getParameter("username");
//        String password = req.getParameter("password");
//        System.out.println("getParameterMap: ");
//        req.getParameterMap().forEach((key, value) -> System.out.println(key + ":" + Arrays.toString(value)));
//        System.out.println("username = " + username);
//        System.out.println("password = " + password);
        logger.debug("username = " + username);
        logger.debug("password = " + password);

        HttpSession session = req.getSession(true);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "СЕССИЯ НЕ НАЙДЕНА");
            return;
        }

        if (userService.isPasswordCorrect(username, password)) {
            System.out.println("Session ID: " + session.getId());

            UserDto user = userService.getUserByLogin(username);

            System.out.println("userId = " + user.getId());

            session.setAttribute("userId", user.getId());
            System.out.println("userId = " + user.getId());
            System.out.println("req.getServletPath() = " + req.getServletPath());
            System.out.println("req.getPathInfo() = " + req.getPathInfo());
            System.out.println("req.getContextPath() = " + req.getContextPath());
            System.out.println(
                    "request.getServletContext().getAttribute(\"loginRedirect\") = " + req.getServletContext().getAttribute("loginRedirect"));
//                resp.sendRedirect(req.getServletPath());
            resp.sendRedirect(req.getContextPath() + req.getServletContext().getAttribute("loginRedirect"));
        } else {
            session.setAttribute("userId", null);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "НЕ АВТОРИЗОВАН");
//            throw new IOException("etetetetet");
        }


    }
}
