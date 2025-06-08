package my.bankapp.service;

import my.bankapp.dto.TransactionCreateDto;
import my.bankapp.dto.TransactionReadDto;
import my.bankapp.exception.AccountNotFoundException;
import my.bankapp.exception.InvalidMoneyValueException;
import my.bankapp.exception.TransactionException;
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
        return new TransactionReadDto(transaction.getId(), transaction.getSenderAccountId(), transaction.getRecipientAccountId(),
                transaction.getMoney(),
                transaction.getTime());
    }

    public Transaction fromDto(TransactionReadDto transactionReadDto) {
        return new Transaction(transactionReadDto.getId(), transactionReadDto.getSenderAccountId(), transactionReadDto.getRecipientUserId(), null,
                transactionReadDto.getMoney(), transactionReadDto.getTime());
    }

    public TransactionReadDto transferMoney(TransactionCreateDto transactionCreateDto, AccountService accountService) {

        long senderAccountId;
        long recipientUserId;
        long recipientAccountId;
        BigDecimal money;

        if (transactionCreateDto.getSenderAccountId() == null && transactionCreateDto.getRecipientUserId() == null) {
            throw new TransactionException("Sender and recipient are not defined");
        }

        if (transactionCreateDto.getSenderAccountId() != null) {
            senderAccountId = transactionCreateDto.getSenderAccountId();
        } else {
            senderAccountId = accountService.getCashAccount().orElseThrow(() -> new AccountNotFoundException("Cash account not found")).getId();
        }
        if (transactionCreateDto.getRecipientUserId() != null) {
            recipientUserId = transactionCreateDto.getRecipientUserId();
            recipientAccountId = accountService.getDefaultAccountByUserId(recipientUserId)
                    .orElseThrow(() -> new AccountNotFoundException("User do not have a default account")).getId();
        } else {
            recipientAccountId = accountService.getCashAccount().orElseThrow(() -> new AccountNotFoundException("Cash account not found")).getId();
            recipientUserId = recipientAccountId;
        }
        if (transactionCreateDto.getMoney() != null && transactionCreateDto.getMoney().compareTo(BigDecimal.ZERO) > 0) {
            money = transactionCreateDto.getMoney();
        } else {
            throw new InvalidMoneyValueException("Money should be more than zero");
        }

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        Connection connection = null;
        RuntimeException mainException = null;
        Transaction transaction = new Transaction(-1, senderAccountId, recipientUserId, recipientAccountId,
                money, currentTime);

        System.out.println("transaction = " + transaction);

        try {
            connection = daoFactory.getDataSource().getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (Statement statement = connection.createStatement()) {
                statement.execute("SET LOCAL lock_timeout = '15s';");
            }

            Account senderAccount = daoFactory.getAccountDao().findById(senderAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("Account with id %s not found".formatted(senderAccountId)));

            senderAccount.subtractValue(money);
            System.out.println("senderAccount = " + senderAccount);
            daoFactory.getAccountDao().update(senderAccount, connection);

            Account recipientAccount = daoFactory.getAccountDao().findById(recipientAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("Account with id %s not found".formatted(senderAccountId)));
            recipientAccount.addValue(money);
            System.out.println("recipientAccount = " + recipientAccount);
            daoFactory.getAccountDao().update(recipientAccount, connection);

            transaction = daoFactory.getTransactionDao().insert(transaction, connection);

            connection.commit();

        } catch (SQLException | RuntimeException sqle) {
            mainException = new TransactionException("Unable to perform transaction", sqle);
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
}
