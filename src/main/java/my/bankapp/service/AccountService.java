package my.bankapp.service;

import my.bankapp.dao.AccountDao;
import my.bankapp.dto.AccountDto;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountService {

    private AccountDao accountDao;
//    private DaoBank accountDao;
    private DaoFactory daoFactory;

    public AccountService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.accountDao = daoFactory.getAccountDao();
    }

    public AccountDto toDto(Account account) throws IllegalArgumentException {
        return new AccountDto(account.getId(), account.getUserId(), account.getType(), account.getBalance(), account.getTitle(),
                account.getIsDefault());
    }

    public Account fromDto(AccountDto accountDto) throws IllegalArgumentException {
        return new Account(accountDto.getId(), accountDto.getUserId(), accountDto.getType(), accountDto.getBalance(), accountDto.getTitle(),
                accountDto.getIsDefault());
    }

    public Account createAccount(long userId, int accountType, String title) throws IllegalArgumentException {

        Account account = new Account(-1, userId, accountType, new BigDecimal("0"), title, false);

        return accountDao.insert(account);
    }

//    public Account createAccount(User user, int accountType, String title) throws IllegalArgumentException {
//        return accountDao.createNewAccount(user.getId(), accountType, new Money("0"), title);
//    }

    public AccountDto getAccountById(long accountId) throws IllegalArgumentException {
        return toDto(accountDao.findById(accountId));
    }

    public AccountDto getCashAccount() throws IllegalArgumentException {
        return getAccountById(-1);
    }

    public List<AccountDto> getAccountList(long userId) {
        return accountDao.findAllByUserId(userId).map(this::toDto).collect(Collectors.toList());
    }

    public Map<Long, AccountDto> getAccountMap(long userId) {
        return accountDao.findAllByUserId(userId).collect(Collectors.toMap(Account::getId, this::toDto));
    }

    public AccountDto updateAccount(AccountDto accountDto) {
        return toDto(accountDao.update(fromDto(accountDto)));
    }

    public AccountDto updateAccount(long accountId, boolean isDefault, String title) {

        AccountDto account = getAccountById(accountId);
        account.setIsDefault(isDefault);
        account.setTitle(title);

        return toDto(accountDao.update(fromDto(account)));
    }

    public AccountDto setUserDefaultAccount(AccountDto accountDto) throws RuntimeException {


        Connection connection = null;
        RuntimeException mainException = null;
        Account account = null;

        BigDecimal currentBalance = null;

        try {
            connection = daoFactory.getDataSource().getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (Statement statement = connection.createStatement()) {
                statement.execute("SET LOCAL lock_timeout = '5s';");
            }

            account = accountDao.updateAccountsDefault(fromDto(accountDto));

            account.setDefault(true);

            account = accountDao.update(account);

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

        return toDto(account);
    }


//    public Account setUserDefaultAccount(long userId, long defaultAccountNumber) throws RuntimeException {
//
//        Account account = getAccountById(defaultAccountNumber);
//        account.setDefault(true);
//
//        return accountDao.update(account);
//    }

//    public boolean setAccountTitle(long accountId, String title) throws RuntimeException {
//        return accountDao.setAccountTitle(accountId, title);
//    }
}
