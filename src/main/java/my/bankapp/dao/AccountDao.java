package my.bankapp.dao;

import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

public class AccountDao implements GenericDao<Account> {
    private final DaoFactory daoFactory;

    public AccountDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    @Override
    public Account findById(long id) {
        Account account = null;
        String sql = "SELECT id, user_id, type, money, is_default, title FROM accounts WHERE id = ? LIMIT 1";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                account = new Account(resultSet.getLong("id"), resultSet.getLong("user_id"), resultSet.getInt("type"),
                        resultSet.getString("title"), resultSet.getBigDecimal("money"), resultSet.getBoolean("is_default"));
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get account %s", id), sqle);
        }

        if (account == null) {
            throw new RuntimeException(String.format("Account %s not found", id));
        }

        return account;
    }

    @Override
    public Stream<Account> findAll() {
        return Stream.empty();
    }

    public Stream<Account> findAllByUserId(long id) {
        Stream.Builder<Account> builder = Stream.builder();

        String sql = "SELECT id, user_id, type, money, is_default, title FROM accounts WHERE user_id = ? ORDER BY is_default DESC, id ASC";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Account account = new Account(resultSet.getLong("id"), resultSet.getLong("user_id"), resultSet.getInt("type"),
                        resultSet.getString("title"), resultSet.getBigDecimal("money"), resultSet.getBoolean("is_default"));
                builder.add(account);
            }
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to get accounts for user %s", id), sqle);
        }

        return builder.build();
    }

    @Override
    public Account insert(Account account) {
        long accountId = -1;
        boolean isDefault = false;
        String sql = """
                    INSERT INTO accounts(user_id, type, money, title, is_default)
                    VALUES (?, ?, ?, ?, NOT EXISTS(SELECT FROM accounts WHERE user_id = ?))
                    RETURNING id, is_default;
                """;

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, account.getUserId());
            statement.setInt(2, account.getType());
            statement.setBigDecimal(3, account.getBalance());
            statement.setString(4, account.getTitle());
            statement.setLong(5, account.getUserId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                accountId = resultSet.getLong("id");
                isDefault = resultSet.getBoolean("is_default");
            }
        } catch (SQLException sqle) {
            throw new RuntimeException("Unable to create account", sqle);
        }

        if (accountId == -1) {
            throw new RuntimeException(String.format("Account for user %s was not created", account.getUserId()));
        } else {
            account.setId(accountId);
            account.setDefault(isDefault);
            return account;
        }
    }

    public Account update(Account account) {

        String sql = "UPDATE accounts SET user_id=?, type=?, money=?, is_default=?, title=? WHERE id=?;";
        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, account.getUserId());
            statement.setInt(2, account.getType());
            statement.setBigDecimal(3, account.getBalance());
            statement.setBoolean(4, account.isDefault());
            statement.setString(5, account.getTitle());
            statement.setLong(6, account.getId());

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException(String.format("Unable to update account %s", account.getId()));
            }

        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to update account %s", account.getId()), sqle);
        }

        return account;
    }

    public Account updateAccountsDefault(Account account) {

        String sql = "UPDATE accounts SET is_default = FALSE WHERE user_id = ? AND is_default = TRUE;";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setLong(1, account.getUserId());

                if (statement.executeUpdate() == 0) {
                    throw new RuntimeException(String.format("Unable to reset default account for user %s", account.getUserId()));
                }
        } catch (SQLException sqle) {
            throw new RuntimeException(String.format("Unable to reset default account for user %s", account.getUserId()), sqle);
        }

        return account;
    }

    public Account updateDefaultAccount(Account account) {

        Connection connection = null;
        RuntimeException mainException = null;
        int updatedRows = 0;

        try {
            connection = daoFactory.getConnection();
            connection.setAutoCommit(false); // Start transaction

            String sql = "UPDATE accounts SET is_default = FALSE WHERE user_id = ? AND is_default = TRUE;";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, account.getUserId());

                statement.executeUpdate();
            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to set default account", sqle);
            }

            sql = "UPDATE accounts SET is_default = TRUE WHERE user_id = ? AND id = ?;";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, account.getUserId());
                statement.setLong(2, account.getId());

                updatedRows = statement.executeUpdate();

                if (updatedRows > 0) {
                    connection.commit();
                } else {
                    throw new RuntimeException(String.format("Account %s was not set as default", account.getId()));
                }
            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to set default account", sqle);
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

        return account;
    }

    @Override
    public boolean delete(long id) {
        return false;
    }
}
