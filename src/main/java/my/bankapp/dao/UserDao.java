package my.bankapp.dao;

import my.bankapp.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

public class UserDao implements GenericDao<User> {
    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User findById(long id) {

        User user = null;
        String sql = "SELECT users.id AS uid, users.login, users.name, credentials.password FROM users " +
                     "LEFT JOIN credentials ON credentials.user_id = users.id " +
                     "WHERE users.id = ? " +
                     "LIMIT 1";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                user = new User(resultSet.getLong("uid"),
                        resultSet.getString("login"),
                        resultSet.getString("name"),
                        resultSet.getString("password"));
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get user %s", id), sqle);
        }

        if (user == null) {
            throw new RuntimeException(String.format("User %s not found", id));
        }

        return user;
    }

    public User findByLogin(String login) {

        User user = null;
        String sql = "SELECT users.id AS uid, users.login, users.name, credentials.password FROM users " +
                     "LEFT JOIN credentials ON credentials.user_id = users.id " +
                     "WHERE users.login = ? " +
                     "LIMIT 1";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                user = new User(resultSet.getLong("uid"),
                        resultSet.getString("login"),
                        resultSet.getString("name"),
                        resultSet.getString("password"));
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get user %s", login), sqle);
        }

        if (user == null) {
            throw new RuntimeException(String.format("User %s not found", login));
        }

        return user;
    }

    @Override
    public Stream<User> findAll() {
        return Stream.empty();
    }

    @Override
    public User insert(User user) {
        Connection connection = null;
        RuntimeException mainException = null;
        long userId = -1;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false); // Start transaction

            String sql = "SET LOCAL lock_timeout = '15s'";

            try (Statement statement = connection.createStatement()) {

                statement.execute(sql);

            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to perform an operation", sqle);
            }

            sql = "INSERT INTO users(login, name) VALUES (?, ?) ON CONFLICT DO NOTHING RETURNING id;";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, user.getLogin());
                statement.setString(2, user.getName());

                ResultSet resultSet = statement.executeQuery();
                resultSet.next();
                userId = resultSet.getLong("id");

            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to create user", sqle);
            }

            sql = "INSERT INTO credentials(user_id, password) VALUES (?, ?) ON CONFLICT (user_id) DO UPDATE SET password = ?;";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getPassword());

                statement.executeUpdate();

                if (statement.executeUpdate() > 0) {
                    connection.commit();
                } else {
                    throw new RuntimeException("No users were created");
                }
            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to set password", sqle);
            }

        } catch (SQLException | RuntimeException sqle) {
            mainException = new RuntimeException("Unable to perform operation", sqle);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    mainException.addSuppressed(rollbackEx);
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    if (mainException != null) {
                        mainException.addSuppressed(e);
                    }
                }
            }
        }

        if (mainException != null) {
            throw mainException;
        }

        if (userId == -1) {
            throw new RuntimeException(String.format("User %s was not created", userId));
        } else {
            return user;
        }
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public boolean delete(long id) {
        return false;
    }

    public String getUserPassword(User user) {

        String hash;

        String sql = "SELECT password FROM credentials WHERE user_id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, user.getId());

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            hash = resultSet.getString("password");
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get password for user %s", user.getId()), sqle);
        }

        if (hash == null) {
            throw new RuntimeException(String.format("User's password %s not found", user.getPassword()));
        }

        return hash;
    }
}
