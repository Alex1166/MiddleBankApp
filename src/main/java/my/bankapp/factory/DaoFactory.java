package my.bankapp.factory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Setter;
import my.bankapp.dao.AccountDao;
import my.bankapp.dao.TransactionDao;
import my.bankapp.dao.UserDao;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DaoFactory {

    private final Logger logger;
    private volatile UserDao userDao;
    private volatile AccountDao accountDao;
    private volatile TransactionDao transactionDao;
    @Setter
    private volatile HikariDataSource dataSource;
    private final HikariConfig config;

    public DaoFactory(Properties properties) {
        this.logger = ServiceFactory.createLogger(DaoFactory.class);
        this.config = new HikariConfig(properties);
    }

    public DaoFactory() {
        this(loadDefaultProperties());
    }

    private static Properties loadDefaultProperties() {
        Properties props = new Properties();
        try (InputStream input = DaoFactory.class.getClassLoader().getResourceAsStream("hikari.properties")) {
            if (input == null) {
                throw new FileNotFoundException("hikari.properties not found");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Hikari properties", e);
        }
        return props;
    }

    public UserDao getUserDao() {
        if (userDao == null) {
            synchronized (this) {
                if (userDao == null) {
                    userDao = new UserDao(this);
                }
            }
        }
        return userDao;
    }

    public AccountDao getAccountDao() {
        if (accountDao == null) {
            synchronized (this) {
                if (accountDao == null) {
                    accountDao = new AccountDao(this);
                }
            }
        }
        return accountDao;
    }

    public TransactionDao getTransactionDao() {
        if (transactionDao == null) {
            synchronized (this) {
                if (transactionDao == null) {
                    transactionDao = new TransactionDao(this);
                }
            }
        }
        return transactionDao;
    }

    public HikariDataSource getDataSource() {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    dataSource = new HikariDataSource(config);
                    testDatabaseConnection(dataSource);
                }
            }
        }
        return dataSource;
    }

    private void testDatabaseConnection(HikariDataSource ds) {
        try (
                Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT now()")) {

            if (rs.next()) {
                logger.info("PostgreSQL version: " + rs.getString(1));
            } else {
                logger.warn("Could not retrieve PostgreSQL version.");
                throw new IllegalStateException("Failed to retrieve PostgreSQL version. Database might be corrupted or unreachable.");
            }
        } catch (Exception e) {
            logger.error("Database test failed", e);
            throw new RuntimeException("Failed to verify DB connection", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
