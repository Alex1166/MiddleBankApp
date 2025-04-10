package my.bankapp.servlet.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.bankapp.dto.AccountDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.service.AccountService;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/accounts/*")
public class AccountsApi extends ApiHttpServlet {

    private ServiceFactory serviceFactory;
    private AccountService accountService;
    private Logger logger;

    @Override
    public void init() throws ServletException {
        Object object = getServletContext().getAttribute("serviceFactory");
        if (object instanceof ServiceFactory) {
            serviceFactory = (ServiceFactory) object;
        } else {
            throw new IllegalStateException();
        }

        accountService = serviceFactory.getAccountService();
        logger = serviceFactory.getLogger();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String info = request.getPathInfo();

        ObjectMapper objectMapper = new ObjectMapper();
        String accountJson;

        long currentUserId = (long) request.getSession().getAttribute("userId");

        if (info == null || info.equals("/")) {
            List<AccountDto> accountList = accountService.getAccountList(currentUserId);
            Map<String, List<AccountDto>> output = new HashMap<>();
            output.put("accounts", accountList);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");

            accountJson = objectMapper.writeValueAsString(output);
        } else {

            long accountId = Long.parseLong(info.split("/")[1]);

            AccountDto accountDto;

            try {
                accountDto = accountService.getAccountById(accountId);
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
                return;
            }

            if (accountDto.getUserId() != currentUserId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "USER DOES NOT HAVE ACCESS TO THIS ACCOUNT");
                return;
            }
            accountJson = objectMapper.writeValueAsString(accountDto);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        PrintWriter writer = response.getWriter();
        writer.print(accountJson);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

//        System.out.println("doPost");
        logger.info("doPost");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(request.getInputStream(), new TypeReference<>() {
        });

        long currentUserId = (long) (request.getSession().getAttribute("userId"));
        String title = (String) data.get("title");

        accountService.createAccount(currentUserId, 0, title);

//        books.put(books.size() + 1, new Book(books.size() + 1, request.getParameter("title"), request.getParameter("description")));

        response.setStatus(HttpServletResponse.SC_ACCEPTED);
    }


    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

//        System.out.println("doPut");
        logger.info("doPut");

        String info = request.getPathInfo();

//        System.out.println("info = " + info);
        logger.info("info = " + info);

        if (info == null || info.equals("/")) {
//            request.setAttribute("books", books);
//            request.getRequestDispatcher("books.jsp").forward(request, response);
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "НЕПРАВИЛЬНЫЙ ЗАПРОС");
//            return;
        } else {

            try {
                System.out.println("accountId = ");

                long currentUserId = (long) (request.getSession().getAttribute("userId"));

                System.out.println("currentUserId = " + currentUserId);

                long accountId = Long.parseLong(info.split("/")[1]);

                System.out.println("accountId = " + accountId);

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data = mapper.readValue(request.getInputStream(), new TypeReference<>() {
                });

                AccountDto accountDto = accountService.getAccountById(accountId);


                if (data.containsKey("default")) {
                    boolean isDefault = Boolean.parseBoolean((String) data.get("default"));
                    accountDto.setIsDefault(isDefault);
                    accountDto = accountService.setUserDefaultAccount(accountDto);
//                    result = accountService.setUserDefaultAccount(currentUserId, accountId);
                }
                if (data.containsKey("title")) {
                    String title = (String) data.get("title");
                    accountDto.setTitle(title);
                    accountDto = accountService.updateAccount(accountDto);
//                    result = result && accountService.setAccountTitle(currentUserId, title);
                }

                System.out.println("accountDto = " + accountDto);
//                if (result) {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);

                ObjectMapper objectMapper = new ObjectMapper();
                String accountJson = objectMapper.writeValueAsString(accountDto);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");

                PrintWriter writer = response.getWriter();
                writer.print(accountJson);
//                } else {
//                    response.setHeader("X-Status-Message", "ОПЕРАЦИЯ НЕ ВЫПОЛНЕНА");
//                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                }
            } catch (RuntimeException e) {
                logger.error(e);
                response.setHeader("X-Status-Message", e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
    }

//    @Override
//    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//
//        System.out.println("doGet");
//
//        String info = req.getPathInfo();
//
//        System.out.println("info = " + info);
//        if (info == null) {
//
//            resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
//            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "НЕПРАВИЛЬНЫЙ ЗАПРОС");
//        } else {
//            if (!books.containsKey(Integer.parseInt(info.replaceAll("/", "")))) {
//                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "КНИГА НЕ НАЙДЕНА");
//            } else {
//                books.remove(Integer.parseInt(info.replaceAll("/", "")));
//                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
//            }
//        }
//    }
}
