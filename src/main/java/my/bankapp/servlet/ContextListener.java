package my.bankapp.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import my.bankapp.factory.ServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebListener // This registers the listener without web.xml
public class ContextListener implements ServletContextListener {


    private static final Logger log = LogManager.getLogger(ContextListener.class);
    private ServiceFactory serviceFactory;
//    private Authentication authentication;

    @Override
    public void contextInitialized(ServletContextEvent event) {
//        System.out.println("🚀 Application Started!");
        log.info("🚀 Application Started!");

        serviceFactory = new ServiceFactory();

        final ServletContext servletContext = event.getServletContext();
        servletContext.setAttribute("appStartTime", System.currentTimeMillis());
        servletContext.setAttribute("serviceFactory", serviceFactory);
        servletContext.setAttribute("loginRedirect", "/");

//        authentication = new AuthenticationService();
//        servletContext.setAttribute("authentication", authentication);
//        servletContext.setAttribute("loginRedirect", "/login");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
//        System.out.println("🛑 Application Stopped!");
        log.info("🛑 Application Stopped!");
    }
}