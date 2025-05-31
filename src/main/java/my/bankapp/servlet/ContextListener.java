package my.bankapp.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import my.bankapp.factory.ServiceFactory;
import org.apache.logging.log4j.Logger;

@WebListener // This registers the listener without web.xml
public class ContextListener implements ServletContextListener {


    private Logger logger;
    private ServiceFactory serviceFactory;
//    private Authentication authentication;

    @Override
    public void contextInitialized(ServletContextEvent event) {
//        System.out.println("🚀 Application Started!");

        serviceFactory = new ServiceFactory();
        logger = ServiceFactory.createLogger(ContextListener.class);
        logger.info("🚀 Application Started!");

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
        logger.info("🛑 Application Stopped!");
        serviceFactory.getDaoFactory().closeDataSource();
    }
}