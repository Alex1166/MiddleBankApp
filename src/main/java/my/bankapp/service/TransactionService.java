package my.bankapp.service;

import my.bankapp.dao.AccountDao;
import my.bankapp.dao.TransactionDao;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionService {

    private DaoFactory daoFactory;
    private TransactionDao transactionDao;
    private AccountDao accountDao;

    //    public TransactionService(TransactionDaoImpl transactionDao, AccountDaoImpl accountDao) {
//        this.transactionDao = transactionDao;
//        this.accountDao = accountDao;
//    }
    public TransactionService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public Transaction transferMoney(long senderAccountId, long recipientAccountId, BigDecimal money) throws IllegalArgumentException {

        Connection connection = null;
        RuntimeException mainException = null;
        Transaction transaction = null;

        BigDecimal currentBalance = null;

        try {
            connection = daoFactory.getDataSource().getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (Statement statement = connection.createStatement()) {
                statement.execute("SET LOCAL lock_timeout = '15s';");
            }

            Account senderAccount = daoFactory.getAccountDao().findById(senderAccountId);
//            currentBalance = senderAccount.getBalance();
//            currentBalance = currentBalance.subtractValue(money);
            senderAccount.subtractValue(money);
            senderAccount = daoFactory.getAccountDao().update(senderAccount);

            Account recipientAccount = daoFactory.getAccountDao().findById(recipientAccountId);
//            currentBalance = recipientAccount.getBalance();
//            currentBalance = currentBalance.addValue(money);
//            recipientAccount.setBalance(currentBalance);
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

        return transactionDao.insert(transaction);
    }
}
