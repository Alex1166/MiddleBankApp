package my.bankapp.service;

import my.bankapp.dao.AccountDao;
import my.bankapp.dto.AccountCreateDto;
import my.bankapp.dto.AccountReadDto;
import my.bankapp.exception.AccountNotFoundException;
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

    private static final long CASH_ACCOUNT_ID = -1L;
    private final AccountDao accountDao;
    private final DaoFactory daoFactory;

    public AccountService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.accountDao = daoFactory.getAccountDao();
    }

    public AccountReadDto toDto(Account account) {
        return new AccountReadDto(account.getId(), account.getUserId(), account.getType(), account.getTitle(), account.getBalance(),
                account.isDefault(), account.isDeleted());
    }

    public Account fromDto(AccountReadDto accountDto) {
        return new Account(accountDto.getId(), accountDto.getUserId(), accountDto.getType(), accountDto.getTitle(), accountDto.getBalance(),
                accountDto.getIsDefault(), accountDto.getIsDeleted());
    }

    public AccountReadDto createAccount(long userId, int accountType, String title) {

        Account account = new Account(-1, userId, accountType, title, new BigDecimal("0"), false, false);

        return toDto(accountDao.insert(account));
    }

    public AccountReadDto createAccount(AccountCreateDto accountCreateDto) {

        Account account = new Account(-1, accountCreateDto.getUserId(), accountCreateDto.getType(), accountCreateDto.getTitle(), new BigDecimal("0"),
                false, false);

        List<AccountReadDto> accountList = getAccountList(accountCreateDto.getUserId());
        if (accountList.isEmpty()) {
            account.setDefault(true);
        }

        return toDto(accountDao.insert(account));
    }

//    public Account createAccount(User user, int accountType, String title) throws IllegalArgumentException {
//        return accountDao.createNewAccount(user.getId(), accountType, new Money("0"), title);
//    }

    public Optional<AccountReadDto> getAccountById(long accountId) {
        Optional<Account> AccountById = accountDao.findById(accountId);
        if (AccountById.isPresent()) {
            return Optional.ofNullable(toDto(AccountById.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<AccountReadDto> getDefaultAccountByUserId(long accountId) {
        Optional<Account> AccountById = accountDao.findDefaultByUserId(accountId);
        if (AccountById.isPresent()) {
            return Optional.ofNullable(toDto(AccountById.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<AccountReadDto> getCashAccount() {
        return getAccountById(CASH_ACCOUNT_ID);
    }

    public List<AccountReadDto> getAccountList(long userId) {
        return accountDao.findAllByUserId(userId).map(this::toDto).collect(Collectors.toList());
    }

    public List<AccountReadDto> getAccountList(GetRequest request) {
        RequestOperation requestOperation = new RequestOperation();
        requestOperation.setOrOperation(false);
        requestOperation.setConditionList(new ArrayList<>());

        RequestCondition requestCondition = new RequestCondition("userId", String.valueOf(request.getUserId()), Long.class, ConditionOperator.EQ);

        requestOperation.getConditionList().add(requestCondition);

        request.getFilterBy().add(requestOperation);
        return accountDao.findAllByParameters(request).map(this::toDto).collect(Collectors.toList());
    }

    public Map<Long, AccountReadDto> getAccountMap(long userId) {
        return accountDao.findAllByUserId(userId).collect(Collectors.toMap(Account::getId, this::toDto));
    }

    public void updateAccount(long id, AccountCreateDto accountCreateDto) {

        Account account = accountDao.findById(id).orElseThrow(() -> new AccountNotFoundException("Account with id %s not found".formatted(id)));

        if (accountCreateDto.getIsDefault() != null) {
            account.setDefault(accountCreateDto.getIsDefault());
        }
        if (accountCreateDto.getType() != null) {
            account.setType(accountCreateDto.getType());
        }
        if (accountCreateDto.getTitle() != null) {
            account.setTitle(accountCreateDto.getTitle());
        }
        System.out.println("account = " + account);

        if (account.isDefault()) {
            accountDao.updateDefaultAccount(account);
        }
        accountDao.update(account);
    }

    public void deleteAccount(long accountId) {

        Account account = accountDao.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account with id %s not found".formatted(accountId)));

        account.setDeleted(true);
        accountDao.update(account);
        if (account.isDefault()) {
            List<AccountReadDto> accountList = getAccountList(account.getUserId());
            if (!accountList.isEmpty()) {
                accountDao.findById(accountList.get(0).getId()).ifPresent((newDefaultAccount)->{
                    newDefaultAccount.setDefault(true);
                    accountDao.update(newDefaultAccount);
                });
            }
        }
    }

    public AccountReadDto setUserDefaultAccount(AccountReadDto accountDto) {

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
