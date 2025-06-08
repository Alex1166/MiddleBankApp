package my.bankapp.dao;

import my.bankapp.exception.DaoException;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.request.GetRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static my.bankapp.dao.DaoUtils.applySqlConditions;
import static my.bankapp.dao.DaoUtils.setPreparedStatementValues;

public class AccountDao implements CreatableDao<Account>, ReadableDao<Account>, UpdatableDao<Account>, DeletableDao<Account> {
    private static final String SELECT_ALL_ACCOUNTS_SQL = """
            SELECT id,
                user_id,
                type,
                money,
                is_default,
                is_deleted,
                title
            FROM accounts
            """;
    private static final String INSERT_ACCOUNT_SQL = """
            INSERT INTO accounts(user_id, type, money, title, is_default)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_ACCOUNT_SQL = """
            UPDATE accounts
            SET user_id=?,
                type=?,
                money=?,
                is_default=?,
                title=?,
                is_deleted=?
            WHERE id=?;
            """;
    private static final String DELETE_ACCOUNT_SQL = """
            UPDATE accounts
            SET is_deleted=true
            WHERE id=?;
            """;

    private final DaoFactory daoFactory;
    private final Map<String, String> fieldsMap;

    public AccountDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;

        fieldsMap = new HashMap<>();
        fieldsMap.put("id", "id");
        fieldsMap.put("userId", "user_id");
        fieldsMap.put("type", "type");
        fieldsMap.put("money", "money");
        fieldsMap.put("isDefault", "is_default");
        fieldsMap.put("isDeleted", "is_deleted");
        fieldsMap.put("title", "title");
    }

    @Override
    public Optional<Account> findById(long id) {
        Account account = null;
        String sql = SELECT_ALL_ACCOUNTS_SQL + " WHERE NOT is_deleted AND id = ?";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                account = new Account(resultSet.getLong("id"), resultSet.getLong("user_id"), resultSet.getInt("type"),
                        resultSet.getString("title"), resultSet.getBigDecimal("money"), resultSet.getBoolean("is_default"),
                        resultSet.getBoolean("is_deleted"));
            }

            return Optional.ofNullable(account);
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get account %s", id), sqle);
        }
    }

    @Override
    public Stream<Account> findAll() {
        return Stream.empty();
    }

    public Stream<Account> findAllByUserId(long userId) {
        Stream.Builder<Account> builder = Stream.builder();

        String sql = SELECT_ALL_ACCOUNTS_SQL + " WHERE NOT is_deleted AND user_id = ? ORDER BY is_default DESC, id ASC";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Account account = new Account(resultSet.getLong("id"), resultSet.getLong("user_id"), resultSet.getInt("type"),
                        resultSet.getString("title"), resultSet.getBigDecimal("money"), resultSet.getBoolean("is_default"),
                        resultSet.getBoolean("is_deleted"));
                builder.add(account);
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get accounts for user %s", userId), sqle);
        }

        return builder.build();
    }

    public Optional<Account> findDefaultByUserId(long userId) {
        Account account = null;

        String sql = SELECT_ALL_ACCOUNTS_SQL + " WHERE NOT is_deleted AND user_id = ? AND is_default";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                account = new Account(resultSet.getLong("id"), resultSet.getLong("user_id"), resultSet.getInt("type"),
                        resultSet.getString("title"), resultSet.getBigDecimal("money"), resultSet.getBoolean("is_default"),
                        resultSet.getBoolean("is_deleted"));
            }

            return Optional.ofNullable(account);
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get default account for user with id %s", userId), sqle);
        }
    }

    @Override
    public Stream<Account> findAllByParameters(GetRequest request) {
        Stream.Builder<Account> builder = Stream.builder();

        StringBuilder sql = new StringBuilder(SELECT_ALL_ACCOUNTS_SQL + " WHERE NOT is_deleted AND ");

        List<DaoValueToInject> daoValuesToInjectList = applySqlConditions(sql, request, fieldsMap);

        try (Connection connection = daoFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            System.out.println("sql = " + sql);
            setPreparedStatementValues(preparedStatement, daoValuesToInjectList);
            System.out.println(preparedStatement);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Account account = new Account(resultSet.getLong("id"), resultSet.getLong("user_id"), resultSet.getInt("type"),
                        resultSet.getString("title"), resultSet.getBigDecimal("money"), resultSet.getBoolean("is_default"),
                        resultSet.getBoolean("is_deleted"));
                builder.add(account);
            }
        } catch (SQLException sqle) {
            throw new RuntimeException("Unable to get accounts", sqle);
        }

        return builder.build();
    }

    @Override
    public Account insert(Account account) {
        try (Connection connection = daoFactory.getConnection()) {
            return insert(account, connection);
        } catch (SQLException sqle) {
            throw new DaoException("Unable to create account", sqle);
        }
    }

    @Override
    public Account insert(Account account, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, account.getUserId());
            preparedStatement.setInt(2, account.getType());
            preparedStatement.setBigDecimal(3, account.getBalance());
            preparedStatement.setString(4, account.getTitle());
            preparedStatement.setBoolean(5, account.isDefault());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (resultSet.next()) {
                account.setId(resultSet.getLong("id"));
            }
            return account;
        } catch (SQLException sqle) {
            throw new DaoException("Unable to create account", sqle);
        }
    }

    @Override
    public void update(Account account) {
        try (Connection connection = daoFactory.getConnection()) {
            update(account, connection);
        } catch (SQLException sqle) {
            throw new DaoException(String.format("Unable to update account %s", account.getId()), sqle);
        }
    }

    public void update(Account account, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT_SQL)) {

            statement.setLong(1, account.getUserId());
            statement.setInt(2, account.getType());
            statement.setBigDecimal(3, account.getBalance());
            statement.setBoolean(4, account.isDefault());
            statement.setString(5, account.getTitle());
            statement.setBoolean(6, account.isDeleted());
            statement.setLong(7, account.getId());

            if (statement.executeUpdate() == 0) {
                throw new DaoException(String.format("Account %s not found", account.getId()));
            }

        } catch (SQLException sqle) {
            throw new DaoException(String.format("Unable to update account %s", account.getId()), sqle);
        }
    }

    public void updateAccountsDefault(Account account) {

        String sql = "UPDATE accounts SET is_default = FALSE WHERE user_id = ? AND is_default = TRUE;";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, account.getUserId());

            if (statement.executeUpdate() == 0) {
                throw new DaoException(String.format("User %s does not have accounts", account.getUserId()));
            }
        } catch (SQLException sqle) {
            throw new DaoException(String.format("Unable to reset default account for user %s", account.getUserId()), sqle);
        }
    }

    public void updateDefaultAccount(Account account) {

        Connection connection = null;
        String setDefaultAllFalseSql = """
                UPDATE accounts
                SET is_default = FALSE
                WHERE user_id = ? AND is_default;
                """;
        String setDefaultTrueSql = """
                UPDATE accounts
                SET is_default = TRUE
                WHERE id = ?;
                """;

        RuntimeException mainException = null;

        try {
            connection = daoFactory.getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement preparedStatement1 = connection.prepareStatement(setDefaultAllFalseSql);
                 PreparedStatement preparedStatement2 = connection.prepareStatement(setDefaultTrueSql)) {
                preparedStatement1.setLong(1, account.getUserId());
                preparedStatement2.setLong(1, account.getId());

                int updatedRows = 0;

                preparedStatement1.executeUpdate();
                updatedRows = preparedStatement2.executeUpdate();

                if (updatedRows > 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }

            } catch (SQLException sqle) {
                throw new DaoException("Unable to set default account", sqle);
            }

        } catch (SQLException | DaoException e) {
            mainException = new DaoException(e.getMessage(), e);
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
    }

//    @Override
//    public boolean delete(long id) {
//        String sql = "DELETE FROM accounts WHERE id = ?";
//
//        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
//
//            statement.setLong(1, id);
//
//            if (statement.executeUpdate() == 0) {
//                throw new RuntimeException(String.format("Unable to delete account %s", id));
//            }
//
//        } catch (SQLException sqle) {
//            throw new RuntimeException(String.format("Unable to delete account %s", id), sqle);
//        }
//
//        return true;
//    }

    @Override
    public void delete(long id) {
        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_ACCOUNT_SQL)) {

            statement.setLong(1, id);

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException(String.format("Unable to delete account %s", id));
            }

        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to delete account %s", id), sqle);
        }
    }
}
