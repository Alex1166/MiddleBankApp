package my.bankapp.factory;

import lombok.Getter;
import my.bankapp.service.AccountService;
import my.bankapp.service.TransactionService;
import my.bankapp.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ServiceFactory {

    @Getter
    private final DaoFactory daoFactory;
    private volatile ControllerFactory controllerFactory;
    private volatile UserService userService;
    private volatile AccountService accountService;
    private volatile TransactionService transactionService;

    public ServiceFactory() {
        daoFactory = new DaoFactory();
    }

    public UserService getUserService() {
        if (userService == null) {
            synchronized (this) {
                if (userService == null) {
                    userService = new UserService(daoFactory);
                }
            }
        }

        return userService;
    }

    public AccountService getAccountService() {
        if (accountService == null) {
            synchronized (this) {
                if (accountService == null) {
                    accountService = new AccountService(daoFactory);
                }
            }
        }

        return accountService;
    }

    public TransactionService getTransactionService() {
        if (transactionService == null) {
            synchronized (this) {
                if (transactionService == null) {
                    transactionService = new TransactionService(daoFactory);
                }
            }
        }

        return transactionService;
    }

    public ControllerFactory getControllerFactory() {
        if (controllerFactory == null) {
            synchronized (this) {
                if (controllerFactory == null) {
                    controllerFactory = new ControllerFactory();
                }
            }
        }

        return controllerFactory;
    }

    public static Logger createLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }
}
