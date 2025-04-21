package my.bankapp.factory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import my.bankapp.dao.AccountDao;
import my.bankapp.dao.TransactionDao;
import my.bankapp.dao.UserDao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DaoFactory {

//    private DaoBank dataBaseService;
    private UserDao userDao;
    private AccountDao accountDao;
    private TransactionDao transactionDao;
    private HikariDataSource dataSource;

//    public DaoBank getDataBaseService() {
//        if (dataBaseService == null) {
//            dataBaseService = new DataBaseService(getDataSource());
//        }
//        return dataBaseService;
//    }

    public UserDao getUserDao() {
        if (userDao == null) {
            userDao = new UserDao(this);
        }
        return userDao;
    }

    public AccountDao getAccountDao() {
        if (accountDao == null) {
            accountDao = new AccountDao(this);
        }
        return accountDao;
    }

    public TransactionDao getTransactionDao() {
        if (transactionDao == null) {
            transactionDao = new TransactionDao(this);
        }
        return transactionDao;
    }

    public HikariDataSource getDataSource() {
        if (dataSource == null) {
            try {
                Properties props = new Properties();
                props.load(DaoFactory.class.getClassLoader().getResourceAsStream("hikari.properties"));

                HikariConfig config = new HikariConfig(props);
                dataSource = new HikariDataSource(config);
                try (
                     Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT version()")) {

                    if (rs.next()) {
                        System.out.println("PostgreSQL version: " + rs.getString(1));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                System.out.println("e = " + e);
                e.printStackTrace();
                throw new RuntimeException("Unable to load DB config", e);
            }
        }
        return dataSource;
    }

//    public DataSource getDataSource() {
//        if (dataSource == null) {
//            try {
//                Context initCtx = new InitialContext();
//                dataSource = (DataSource) initCtx.lookup("java:comp/env/jdbc/PostgresDB");
//            } catch (NamingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return dataSource;
//    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
