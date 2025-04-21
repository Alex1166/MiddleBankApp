package my.bankapp.service;

import my.bankapp.dto.TransactionDto;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionService {
    private final DaoFactory daoFactory;

    public TransactionService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public TransactionDto toDto(Transaction transaction) throws IllegalArgumentException {
        return new TransactionDto(transaction.getId(), transaction.getSenderAccountId(), transaction.getRecipientAccountId(), transaction.getMoney());
    }

    public Transaction fromDto(TransactionDto transactionDto) throws IllegalArgumentException {
        return new Transaction(transactionDto.getId(), transactionDto.getSenderAccountId(), transactionDto.getRecipientAccountId(), transactionDto.getMoney());
    }

    public TransactionDto transferMoney(TransactionDto transactionDto) throws IllegalArgumentException {
        return toDto(transferMoney(transactionDto.getSenderAccountId(), transactionDto.getRecipientAccountId(), transactionDto.getMoney()));
    }

    public Transaction transferMoney(long senderAccountId, long recipientAccountId, BigDecimal money) throws IllegalArgumentException {

        Connection connection = null;
        RuntimeException mainException = null;
        Transaction transaction = null;

        try {
            connection = daoFactory.getDataSource().getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (Statement statement = connection.createStatement()) {
                statement.execute("SET LOCAL lock_timeout = '15s';");
            }

            Account senderAccount = daoFactory.getAccountDao().findById(senderAccountId);
            senderAccount.subtractValue(money);
            senderAccount = daoFactory.getAccountDao().update(senderAccount);

            Account recipientAccount = daoFactory.getAccountDao().findById(recipientAccountId);
            senderAccount.addValue(money);
            recipientAccount = daoFactory.getAccountDao().update(recipientAccount);

            transaction = saveTransaction(senderAccount.getId(), recipientAccount.getId(), money);

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

        return transaction;
    }

    public Transaction saveTransaction(long senderAccountId, long recipientAccountId, BigDecimal money) throws IllegalArgumentException {

        Transaction transaction = new Transaction(-1, senderAccountId, recipientAccountId, money);

        return daoFactory.getTransactionDao().insert(transaction);
    }
}
