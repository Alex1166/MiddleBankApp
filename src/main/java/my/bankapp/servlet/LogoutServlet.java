package my.bankapp.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/v1/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        System.out.println("username = " + username);

        HttpSession session = req.getSession(false);

        if (session != null) {
            System.out.println("session is invalidated = " + session.getId());
            session.invalidate(); // Destroy session
        }
//        resp.setStatus(HttpServletResponse.SC_OK);
//        resp.sendError(HttpServletResponse.SC_OK, "НЕ АВТОРИЗОВАН");
        resp.sendRedirect(req.getContextPath() + "/login");

    }
}
