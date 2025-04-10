package my.bankapp.dao;

import javax.sql.DataSource;

public class DataBaseService {
    private final DataSource dataSource;

    public DataBaseService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

//    public boolean setAccountMoney(long accountId, TransactionService mh, Money money, MoneyOperationFunction<Money, Money> function) {
//
//        Connection connection = null;
//        Money currentWallet = null;
//        RuntimeException mainException = null;
//
//        try {
//            connection = getConnection();
//            connection.setAutoCommit(false); // Start transaction
//
//            String sql = "SET LOCAL lock_timeout = '15s'";
//
//            try (Statement statement = connection.createStatement()) {
//
//                statement.execute(sql);
//
//            } catch (SQLException sqle) {
//                throw new RuntimeException("Unable to perform an operation", sqle);
//            }
//
//            sql = "SELECT money FROM accounts WHERE id=? FOR UPDATE;";
//
//            try (PreparedStatement statement = connection.prepareStatement(sql)) {
//
//                statement.setLong(1, accountId);
//
//                ResultSet resultSet = statement.executeQuery();
//
//                resultSet.next();
//                currentWallet = new Money(resultSet.getBigDecimal("money"));
//
//            } catch (SQLException sqle) {
//                throw new RuntimeException("Unable to get account wallet", sqle);
//            }
//
//            sql = "UPDATE accounts SET money=? WHERE id=?;";
//
//            try (PreparedStatement statement = connection.prepareStatement(sql)) {
//                statement.setBigDecimal(1, function.applyAsMoney(currentWallet, money).getValue());
//                statement.setLong(2, accountId);
//
//                statement.executeUpdate();
//
//                if (statement.executeUpdate() > 0) {
//                    connection.commit();
//                } else {
//                    throw new RuntimeException("No account wallets were changed");
//                }
//            } catch (SQLException sqle) {
//                throw new RuntimeException("Unable to change account wallet", sqle);
//            }
//
//        } catch (SQLException | RuntimeException sqle) {
//            mainException = new RuntimeException("Unable to perform operation", sqle);
//            if (connection != null) {
//                try {
//                    connection.rollback();
//                } catch (SQLException rollbackEx) {
//                    mainException.addSuppressed(rollbackEx);
//                }
//            }
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (SQLException e) {
//                    if (mainException != null) {
//                        mainException.addSuppressed(e);
//                    }
//                }
//            }
//        }
//
//        if (mainException != null) {
//            throw mainException;
//        }
//
//        if (accountId == -1) {
//            throw new RuntimeException(String.format("Account %s wallet was not changed", accountId));
//        } else {
//            return true;
//        }
//    }
}