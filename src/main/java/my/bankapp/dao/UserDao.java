package my.bankapp.dao;

import my.bankapp.exception.DaoException;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.Transaction;
import my.bankapp.model.User;
import my.bankapp.model.request.GetRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.stream.Stream;

public class UserDao implements CreatableDao<User>, ReadableDao<User>, UpdatableDao<User>, DeletableDao<User> {
    private static final String SELECT_USERS_BY_ID_SQL = """
            SELECT users.id AS uid,
                users.login,
                users.name,
                users.is_deleted,
                credentials.password
            FROM users
            LEFT JOIN credentials
            ON credentials.user_id = users.id
            WHERE users.id = ?
            """;
    private static final String SELECT_USER_ID_BY_LOGIN_SQL = """
            SELECT id
            FROM users
            WHERE login = ?
            """;
    private static final String INSERT_USER_SQL = """
            INSERT INTO users(login, name)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;
    private static final String INSERT_PASSWORD_SQL = """
            INSERT INTO credentials(user_id, password)
            VALUES (?, ?)
            ON CONFLICT (user_id) DO UPDATE SET password = ?
            """;
    private static final String UPDATE_USER_SQL = """
            UPDATE users
                SET login=?,
                name=?,
                is_deleted=?
            WHERE id=?;
            """;
    private static final String DELETE_USER_SQL = """
            UPDATE users
            SET is_deleted=true
            WHERE id=?;
            """;

    private final DaoFactory daoFactory;

    public UserDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    @Override
    public Optional<User> findById(long id) {

        User user = null;

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_USERS_BY_ID_SQL)) {

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                user = new User(resultSet.getLong("uid"),
                        resultSet.getString("login"),
                        resultSet.getString("name"),
                        resultSet.getString("password"), resultSet.getBoolean("is_deleted"));
            }

            return Optional.ofNullable(user);
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get user %s", id), sqle);
        }
    }

    public Optional<Long> findByLogin(String login) {

        Long id = null;

        try (Connection connection = daoFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_ID_BY_LOGIN_SQL)) {

            statement.setString(1, login);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                id = resultSet.getLong("id");
            }

            return Optional.ofNullable(id);
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to find user %s", login), sqle);
        }
    }

    @Override
    public Stream<User> findAll() {
        return Stream.empty();
    }

    @Override
    public User insert(User user) {
        Connection connection = null;
        try {
            connection = daoFactory.getConnection();
            return insert(user, connection);
        } catch (SQLException sqle) {
            throw new DaoException("Unable to save transaction", sqle);
        }
    }

    @Override
    public User insert(User user, Connection connection) {
        RuntimeException mainException = null;
        long userId = -1;

        try {
            connection.setAutoCommit(false); // Start transaction

            String sql = "SET LOCAL lock_timeout = '15s'";

            try (Statement statement = connection.createStatement()) {

                statement.execute(sql);

            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to perform an operation", sqle);
            }

            try (PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, user.getLogin());
                statement.setString(2, user.getName());

                statement.executeQuery();

                ResultSet resultSet = statement.getGeneratedKeys();

                if (resultSet.next()) {
                    userId = resultSet.getLong("id");
                }

            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to create user", sqle);
            }


            try (PreparedStatement statement = connection.prepareStatement(INSERT_PASSWORD_SQL)) {
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
            user.setId(userId);
            return user;
        }
    }

    @Override
    public void update(User user) {
        try (Connection connection = daoFactory.getConnection()) {
            update(user, connection);
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to update user %s", user.getId()), sqle);
        }
    }

    @Override
    public void update(User user, Connection connection) {

        try (PreparedStatement statement = connection.prepareStatement(UPDATE_USER_SQL)) {

            statement.setString(1, user.getLogin());
            statement.setString(2, user.getName());
            statement.setBoolean(3, user.isDeleted());
            statement.setLong(4, user.getId());

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException(String.format("User %s not found", user.getId()));
            }

        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to update user %s", user.getId()), sqle);
        }
    }

    @Override
    public Stream<User> findAllByParameters(GetRequest request) {
        return Stream.empty();
    }

    public String getUserPassword(User user) {

        String hash;

        String sql = "SELECT password FROM credentials WHERE user_id = ?";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, user.getId());

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            hash = resultSet.getString("password");
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get password for user %s", user.getId()), sqle);
        }

        if (hash == null) {
            throw new RuntimeException(String.format("User's password %s not found", user.getId()));
        }

        return hash;
    }

    public void setUserPassword(User user) {

        String sql = "UPDATE credentials SET password = ? WHERE user_id = ?;";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getPassword());
            statement.setLong(2, user.getId());

            statement.executeUpdate();

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException(String.format("User's %s password not changed", user.getId()));
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("User's %s password not changed", user.getId()));
        }
    }

//    @Override
//    public void delete(long id) {
//        String sql = "DELETE FROM users WHERE id = ?";
//
//        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
//
//            statement.setLong(1, id);
//
//            System.out.println("id = " + id);
//
//            if (statement.executeUpdate() == 0) {
//                throw new RuntimeException(String.format("Unable to delete user %s", id));
//            }
//
//        } catch (SQLException sqle) {
//            throw new RuntimeException(String.format("Unable to delete user %s", id), sqle);
//        }
//    }

    @Override
    public void delete(long id) {
        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_USER_SQL)) {

            statement.setLong(1, id);

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException(String.format("Unable to delete user %s", id));
            }

        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to delete user %s", id), sqle);
        }
    }
}
