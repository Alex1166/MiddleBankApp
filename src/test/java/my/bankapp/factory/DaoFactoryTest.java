package my.bankapp.factory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoFactoryTest {

    private DaoFactory daoFactory;

    @BeforeEach
    void setup() {
        Properties props = new Properties();
        props.setProperty("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        props.setProperty("username", "sa");
        props.setProperty("password", "");
        props.setProperty("driverClassName", "org.h2.Driver");

        daoFactory = new DaoFactory(props);
    }

    @AfterEach
    void close() {
        daoFactory.closeDataSource();
    }

    @Test
    public void getDataSource() {
    }

    @Test
    public void getConnectionH2ShouldBeNotNullTest() throws SQLException {
        assertNotNull(daoFactory.getConnection());
    }

    @Test
    public void getConnectionShouldBeNotNullTest() throws SQLException {
        daoFactory = new DaoFactory();
        assertNotNull(daoFactory.getConnection());
        assertDoesNotThrow(()->{daoFactory.getConnection();});
    }

    @Test
    public void getConnectionShouldThrowError() throws SQLException {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.user", "a");
        props.setProperty("dataSource.password", "b");
        props.setProperty("dataSource.databaseName", "c");
        props.setProperty("dataSource.portNumber", "5432");
        props.setProperty("dataSource.serverName", "localhost");

        daoFactory = new DaoFactory(props);
        assertThrows(RuntimeException.class, ()->{daoFactory.getConnection();});
    }

    @Test
    public void closeDataSource() {
    }

    @Test
    public void setDataSource() {
    }
}