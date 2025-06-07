package my.bankapp.service;

import my.bankapp.dto.TransactionReadDto;
import my.bankapp.exception.AccountNotFoundException;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.Transaction;
import my.bankapp.model.request.GetRequest;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionService {
    private final DaoFactory daoFactory;

    public TransactionService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public Optional<TransactionReadDto> getTransactionById(long transactionId) {
        if (daoFactory.getTransactionDao().findById(transactionId).isPresent()) {
            return Optional.ofNullable(toDto(daoFactory.getTransactionDao().findById(transactionId).get()));
        } else {
            return Optional.empty();
        }
    }

    public List<TransactionReadDto> getTransactionList(long accountId) {
        return daoFactory.getTransactionDao().findAllByAccountId(accountId).map(this::toDto).collect(Collectors.toList());
    }

    public List<TransactionReadDto> getTransactionList(GetRequest request) {
//        request.getFilterBy().put("senderAccountId", List.of(String.valueOf(request.getUserId())));
//        request.getFilterBy().put("recipientAccountId", List.of(String.valueOf(request.getUserId())));

        return daoFactory.getTransactionDao().findAllByParameters(request).map(this::toDto).collect(Collectors.toList());
    }

    public TransactionReadDto toDto(Transaction transaction) {
        return new TransactionReadDto(transaction.getId(), transaction.getSenderAccountId(), transaction.getRecipientAccountId(), transaction.getMoney(),
                transaction.getTime());
    }

    public Transaction fromDto(TransactionReadDto transactionReadDto) {
        return new Transaction(transactionReadDto.getId(), transactionReadDto.getSenderAccountId(), transactionReadDto.getRecipientAccountId(),
                transactionReadDto.getMoney(), transactionReadDto.getTime());
    }

    public TransactionReadDto transferMoney(TransactionReadDto transactionReadDto, AccountService accountService) {

        long senderAccountId;
        long recipientAccountId;
        BigDecimal money;

        if (transactionReadDto.getSenderAccountId() != null) {
            senderAccountId = transactionReadDto.getSenderAccountId();
        } else {
            senderAccountId = accountService.getCashAccount().orElseThrow(() -> new AccountNotFoundException("Cash account not found")).getId();
        }
        if (transactionReadDto.getRecipientAccountId() != null) {
            recipientAccountId = transactionReadDto.getRecipientAccountId();
        } else {
            recipientAccountId = accountService.getCashAccount().orElseThrow(() -> new AccountNotFoundException("Cash account not found")).getId();
        }
        if (transactionReadDto.getMoney() != null && transactionReadDto.getMoney().compareTo(BigDecimal.ZERO) > 0) {
            money = transactionReadDto.getMoney();
        } else {
            throw new RuntimeException();
        }

        Connection connection = null;
        RuntimeException mainException = null;
        Transaction transaction = fromDto(transactionReadDto);

        try {
            connection = daoFactory.getDataSource().getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (Statement statement = connection.createStatement()) {
                statement.execute("SET LOCAL lock_timeout = '15s';");
            }

            Optional<Account> senderAccount = daoFactory.getAccountDao().findById(senderAccountId);

            senderAccount.orElseThrow(() -> new AccountNotFoundException("Account with id %s not found".formatted(senderAccountId))).subtractValue(money);
            daoFactory.getAccountDao().update(senderAccount.get());

            Optional<Account> recipientAccount = daoFactory.getAccountDao().findById(recipientAccountId);
            recipientAccount.orElseThrow(() -> new AccountNotFoundException("Account with id %s not found".formatted(senderAccountId))).addValue(money);
            daoFactory.getAccountDao().update(recipientAccount.get());

            transaction = saveTransaction(transactionReadDto);

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

        return toDto(transaction);
    }

    public Transaction saveTransaction(TransactionReadDto transactionReadDto) throws IllegalArgumentException {

        Transaction transaction = new Transaction(-1, transactionReadDto.getSenderAccountId(), transactionReadDto.getRecipientAccountId(),
                transactionReadDto.getMoney(), new Timestamp(System.currentTimeMillis()));

        return daoFactory.getTransactionDao().insert(transaction);
    }
}
