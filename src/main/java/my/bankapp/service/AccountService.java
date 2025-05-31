package my.bankapp.service;

import my.bankapp.dao.AccountDao;
import my.bankapp.dto.AccountDto;
import my.bankapp.exception.DaoException;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.request.ConditionOperator;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.request.RequestCondition;
import my.bankapp.model.request.RequestOperation;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountService {

    private final AccountDao accountDao;
    private final DaoFactory daoFactory;

    private static final long CASH_ACCOUNT_ID = -1L;

    public AccountService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.accountDao = daoFactory.getAccountDao();
    }

    public AccountDto toDto(Account account) {
        return new AccountDto(account.getId(), account.getUserId(), account.getType(), account.getTitle(), account.getBalance(),
                account.isDefault(), account.isDeleted());
    }

    public Account fromDto(AccountDto accountDto) {
        return new Account(accountDto.getId(), accountDto.getUserId(), accountDto.getType(), accountDto.getTitle(), accountDto.getBalance(),
                accountDto.getIsDefault(), accountDto.getIsDeleted());
    }

    public AccountDto createAccount(long userId, int accountType, String title) {

        Account account = new Account(-1, userId, accountType, title, new BigDecimal("0"), false, false);

        return toDto(accountDao.insert(account));
    }

    public AccountDto createAccount(AccountDto accountDto) {

        Account account = new Account(-1, accountDto.getUserId(), accountDto.getType(), accountDto.getTitle(), new BigDecimal("0"), false, false);

        List<AccountDto> accountList = getAccountList(accountDto.getUserId());
        if (accountList.isEmpty()) {
            account.setDefault(true);
        }

        return toDto(accountDao.insert(account));
    }

//    public Account createAccount(User user, int accountType, String title) throws IllegalArgumentException {
//        return accountDao.createNewAccount(user.getId(), accountType, new Money("0"), title);
//    }

    public Optional<AccountDto> getAccountById(long accountId) {
        if (accountDao.findById(accountId).isPresent()) {
            return Optional.ofNullable(toDto(accountDao.findById(accountId).get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<AccountDto> getCashAccount() {
        return getAccountById(CASH_ACCOUNT_ID);
    }

    public List<AccountDto> getAccountList(long userId) {
        return accountDao.findAllByUserId(userId).map(this::toDto).collect(Collectors.toList());
    }

    public List<AccountDto> getAccountList(GetRequest request) {
        RequestOperation requestOperation = new RequestOperation();
        requestOperation.setOrOperation(false);
        requestOperation.setConditionList(new ArrayList<>());

        RequestCondition requestCondition = new RequestCondition("userId", String.valueOf(request.getUserId()), Long.class, ConditionOperator.EQ);

        requestOperation.getConditionList().add(requestCondition);

        request.getFilterBy().add(requestOperation);
        return accountDao.findAllByParameters(request).map(this::toDto).collect(Collectors.toList());
    }

    public Map<Long, AccountDto> getAccountMap(long userId) {
        return accountDao.findAllByUserId(userId).collect(Collectors.toMap(Account::getId, this::toDto));
    }

    public void updateAccount(AccountDto accountDto) {

        Account account = fromDto(accountDto);

        if (accountDto.getIsDefault()) {
            accountDao.updateDefaultAccount(account);
        }
        accountDao.update(account);
    }

    public boolean deleteAccount(long accountId) {
        Optional<AccountDto> accountDto = getAccountById(accountId);

        if (accountDto.isEmpty()) {
            return false;
        }

        accountDto.get().setIsDefault(true);
        accountDao.update(fromDto(accountDto.get()));
//        if (accountDao.delete(accountId)) {
        if (accountDto.get().getIsDefault()) {
            List<AccountDto> accountList = getAccountList(accountDto.get().getUserId());
            if (!accountList.isEmpty()) {
                accountDto = getAccountById(accountList.get(0).getId());
                if (accountDto.isPresent()) {
                    accountDto.get().setIsDefault(true);
                    updateAccount(accountDto.get());
                }
            }
        }
        return true;
//        }
//        return false;
    }

    public AccountDto setUserDefaultAccount(AccountDto accountDto) {

        Connection connection = null;
        RuntimeException mainException = null;
        Account account = fromDto(accountDto);

        BigDecimal currentBalance = null;

        try {
            connection = daoFactory.getDataSource().getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (Statement statement = connection.createStatement()) {
                statement.execute("SET LOCAL lock_timeout = '5s';");
            }

            accountDao.updateAccountsDefault(account);

            account.setDefault(true);

            accountDao.update(account);

            connection.commit();

        } catch (SQLException sqle) {
            mainException = new DaoException("Unable to perform operation", sqle);
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
