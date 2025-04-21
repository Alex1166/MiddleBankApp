package my.bankapp.dao;

import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

public class TransactionDao implements GenericDao<Transaction> {
    private final DaoFactory daoFactory;

    public TransactionDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    @Override
    public Transaction findById(long id) {
        return null;
    }

    @Override
    public Stream<Transaction> findAll() {
        return Stream.empty();
    }

    @Override
    public Transaction insert(Transaction transaction) {
        long transactionId = -1;
        String sql = """
                    INSERT INTO transactions(sender_account_id, recipient_account_id, money)
                    VALUES (?, ?, ?)
                    RETURNING id;
                """;

        try (Connection connection = daoFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, transaction.getSenderAccountId());
            statement.setLong(2, transaction.getRecipientAccountId());
            statement.setBigDecimal(3, transaction.getMoney());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                transactionId = resultSet.getLong("id");
            }
        } catch (SQLException sqle) {
            throw new RuntimeException("Unable to save transaction", sqle);
        }

        if (transactionId == -1) {
            throw new RuntimeException(String.format("Transaction %s ->  %s was not created", transaction.getSenderAccountId(), transaction.getRecipientAccountId()));
        } else {
            return transaction;
        }
    }

    @Override
    public Transaction update(Transaction transaction) {
        return null;
    }

    @Override
    public boolean delete(long id) {
        return false;
    }
}
