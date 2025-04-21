package my.bankapp.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/api/v1/*", "/login", "/register", "/profile", "/transfer"})
public class AuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath();

        System.out.println("path = " + path);

        HttpSession session = req.getSession(false);

        if (path.endsWith(".js")) {
            chain.doFilter(req, resp);
        } else {
            if (path.startsWith("/login") || path.startsWith("/register") || ((HttpServletRequest) request).getMethod().equals("POST") && (path.contains("/login") || path.contains("/register"))) {
                if (session == null || session.getAttribute("userId") == null) {
                    chain.doFilter(req, resp);
                } else {
                    req.getRequestDispatcher("/").forward(req, resp);
                }
            } else {
                if (session == null || session.getAttribute("userId") == null) {
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                } else {
                    chain.doFilter(req, resp);
                }
            }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
