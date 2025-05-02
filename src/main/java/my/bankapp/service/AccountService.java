package my.bankapp.service;

import my.bankapp.dao.AccountDao;
import my.bankapp.dto.AccountDto;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.request.GetRequest;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountService {

    private final AccountDao accountDao;
    private final DaoFactory daoFactory;

    public AccountService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.accountDao = daoFactory.getAccountDao();
    }

    public AccountDto toDto(Account account) throws IllegalArgumentException {
        return new AccountDto(account.getId(), account.getUserId(), account.getType(), account.getTitle(), account.getBalance(),
                account.isDefault(), account.isDeleted());
    }

    public Account fromDto(AccountDto accountDto) throws IllegalArgumentException {
        return new Account(accountDto.getId(), accountDto.getUserId(), accountDto.getType(), accountDto.getTitle(), accountDto.getBalance(),
                accountDto.isDefault(), accountDto.isDeleted());
    }

    public AccountDto createAccount(long userId, int accountType, String title) throws IllegalArgumentException {

        Account account = new Account(-1, userId, accountType, title, new BigDecimal("0"), false, false);

        return toDto(accountDao.insert(account));
    }

    public AccountDto createAccount(AccountDto accountDto) throws IllegalArgumentException {

        Account account = new Account(-1, accountDto.getUserId(), accountDto.getType(), accountDto.getTitle(), new BigDecimal("0"), false, false);

        return toDto(accountDao.insert(account));
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

    public List<AccountDto> getAccountList(GetRequest request) {
        request.getFilterBy().put("userId", List.of(String.valueOf(request.getUserId())));
        return accountDao.findAllByParameters(request).map(this::toDto).collect(Collectors.toList());
    }

    public Map<Long, AccountDto> getAccountMap(long userId) {
        return accountDao.findAllByUserId(userId).collect(Collectors.toMap(Account::getId, this::toDto));
    }

    public AccountDto updateAccount(AccountDto accountDto) {

        if (accountDto.isDefault()) {
            accountDao.updateAccountsDefault(fromDto(accountDto));
        }
        return toDto(accountDao.update(fromDto(accountDto)));
    }

    public boolean deleteAccount(long accountId) throws IllegalArgumentException {
        AccountDto accountDto = getAccountById(accountId);
        accountDto.setDeleted(true);
        accountDao.update(fromDto(accountDto));
//        if (accountDao.delete(accountId)) {
        if (accountDto.isDefault()) {
            List<AccountDto> accountList = getAccountList(accountDto.getUserId());
            if (!accountList.isEmpty()) {
                accountDto = getAccountById(accountList.get(0).getId());
                accountDto.setDefault(true);
                updateAccount(accountDto);
            }
        }
        return true;
//        }
//        return false;
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
}
