package my.bankapp.factory;

import my.bankapp.dao.AccountDao;
import my.bankapp.dao.TransactionDao;
import my.bankapp.dao.UserDao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DaoFactory {

//    private DaoBank dataBaseService;
    private UserDao userDao;
    private AccountDao accountDao;
    private TransactionDao transactionDao;
    private DataSource dataSource;

//    public DaoBank getDataBaseService() {
//        if (dataBaseService == null) {
//            dataBaseService = new DataBaseService(getDataSource());
//        }
//        return dataBaseService;
//    }

    public UserDao getUserDao() {
        if (userDao == null) {
            userDao = new UserDao(getDataSource());
        }
        return userDao;
    }

    public AccountDao getAccountDao() {
        if (accountDao == null) {
            accountDao = new AccountDao(getDataSource());
        }
        return accountDao;
    }

    public TransactionDao getTransactionDao() {
        if (transactionDao == null) {
            transactionDao = new TransactionDao(getDataSource());
        }
        return transactionDao;
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            try {
                Context initCtx = new InitialContext();
                dataSource = (DataSource) initCtx.lookup("java:comp/env/jdbc/PostgresDB");
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
        return dataSource;
    }

    private Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
