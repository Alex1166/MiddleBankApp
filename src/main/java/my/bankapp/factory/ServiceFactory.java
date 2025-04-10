package my.bankapp.factory;

import my.bankapp.service.AccountService;
import my.bankapp.service.TransactionService;
import my.bankapp.service.UserService;
import my.bankapp.servlet.ContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServiceFactory {

    private final DaoFactory daoFactory;
    private UserService userService;
    private AccountService accountService;
    private TransactionService transactionService;
    private Logger logger;

    public ServiceFactory() {
        daoFactory = new DaoFactory();
    }

    public UserService getUserService() {
        if (userService == null) {
            userService = new UserService(daoFactory.getUserDao());
        }

        return userService;
    }

    public AccountService getAccountService() {
        if (accountService == null) {
            accountService = new AccountService(daoFactory);
        }

        return accountService;
    }

    public TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = new TransactionService(daoFactory);
        }

        return transactionService;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(ContextListener.class);
        }

        return logger;
    }
}
