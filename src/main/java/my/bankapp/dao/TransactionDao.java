package my.bankapp.dao;

import my.bankapp.exception.DaoException;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.Transaction;
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

public class TransactionDao implements CreatableDao<Transaction>, ReadableDao<Transaction> {
    private static final String SELECT_ALL_TRANSACTIONS_SQL = """
            SELECT id,
                sender_account_id,
                recipient_user_id,
                recipient_account_id,
                money,
                time
            FROM transactions
            """;
    private static final String INSERT_TRANSACTION_SQL = """
            INSERT INTO transactions(sender_account_id, recipient_user_id, recipient_account_id, money, time)
                VALUES (?, ?, ?, ?, ?)
            """;
    private final DaoFactory daoFactory;
    private final Map<String, String> fieldsMap;

    public TransactionDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;

        fieldsMap = new HashMap<>();
        fieldsMap.put("id", "id");
        fieldsMap.put("senderAccountId", "sender_account_id");
        fieldsMap.put("recipientUserId", "recipient_user_id");
        fieldsMap.put("recipientAccountId", "recipient_account_id");
        fieldsMap.put("money", "money");
        fieldsMap.put("time", "time");
    }

    @Override
    public Optional<Transaction> findById(long id) {
        String sql = SELECT_ALL_TRANSACTIONS_SQL + " WHERE id = ?";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            Transaction transaction = null;

            while (resultSet.next()) {
                transaction = new Transaction(resultSet.getLong("id"), resultSet.getLong("sender_account_id"),
                        resultSet.getLong("recipient_user_id"),
                        resultSet.getLong("recipient_account_id"), resultSet.getBigDecimal("money")
                        , resultSet.getTimestamp("time"));
            }

            return Optional.ofNullable(transaction);
        } catch (SQLException sqle) {
            throw new DaoException(String.format("Unable to get transaction %s", id), sqle);
        }
    }

    @Override
    public Stream<Transaction> findAll() {
        return Stream.empty();
    }

    @Override
    public Transaction insert(Transaction transaction) {
        try (Connection connection = daoFactory.getConnection()) {
            return insert(transaction, connection);
        } catch (SQLException sqle) {
            throw new DaoException("Unable to save transaction", sqle);
        }
    }

    @Override
    public Transaction insert(Transaction transaction, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TRANSACTION_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, transaction.getSenderAccountId());
            preparedStatement.setLong(2, transaction.getRecipientUserId());
            preparedStatement.setLong(3, transaction.getRecipientAccountId());
            preparedStatement.setBigDecimal(4, transaction.getMoney());
            preparedStatement.setTimestamp(5, transaction.getTime());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            while (resultSet.next()) {
                transaction.setId(resultSet.getLong("id"));
            }
            return transaction;
        } catch (SQLException sqle) {
            throw new DaoException("Unable to save transaction", sqle);
        }
    }

    public Stream<Transaction> findAllByAccountId(long id) {
        Stream.Builder<Transaction> builder = Stream.builder();

        String sql = SELECT_ALL_TRANSACTIONS_SQL + " WHERE sender_account_id = ? OR recipient_account_id = ? ORDER BY time DESC";

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Transaction transaction = new Transaction(resultSet.getLong("id"), resultSet.getLong("sender_account_id"),
                        resultSet.getLong("recipient_user_id"),
                        resultSet.getLong("recipient_account_id"), resultSet.getBigDecimal("money"),
                        resultSet.getTimestamp("time"));
                builder.add(transaction);
            }
        } catch (SQLException sqle) {
            throw new DaoException(String.format("Unable to get transactions for account %s", id), sqle);
        }

        return builder.build();
    }

    @Override
    public Stream<Transaction> findAllByParameters(GetRequest request) {
        Stream.Builder<Transaction> builder = Stream.builder();

        StringBuilder sql = new StringBuilder(SELECT_ALL_TRANSACTIONS_SQL);

        List<DaoValueToInject> daoValuesToInjectList = applySqlConditions(sql, request, fieldsMap);

        try (Connection connection = daoFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            System.out.println("sql = " + sql);
            setPreparedStatementValues(preparedStatement, daoValuesToInjectList);
            System.out.println(preparedStatement);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Transaction transaction = new Transaction(resultSet.getLong("id"), resultSet.getLong("sender_account_id"),
                        resultSet.getLong("recipient_user_id"),
                        resultSet.getLong("recipient_account_id"), resultSet.getBigDecimal("money")
                        , resultSet.getTimestamp("time"));
                builder.add(transaction);
            }
        } catch (SQLException sqle) {
            throw new DaoException("Unable to get transactions", sqle);
        }

        return builder.build();
    }
}
