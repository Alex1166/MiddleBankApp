package my.bankapp.controller;

import my.bankapp.dto.TransactionDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.request.TransactionRequest;
import my.bankapp.model.response.ControllerResponse;
import my.bankapp.model.response.PaginatedResponse;

import java.util.List;
import java.util.Optional;

public class TransactionsController
        implements ReadableController<TransactionDto, TransactionRequest>, CreatableController<TransactionDto, TransactionRequest>,
        UpdatableController<TransactionDto, TransactionRequest>, DeletableController<TransactionDto, TransactionRequest> {
    @Override
    public ControllerResponse<List<TransactionDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("TransactionsController processGetAll");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getTransactionService().getTransactionList(id));
    }

    @Override
    public PaginatedResponse<List<TransactionDto>> processGetAll(GetRequest request, ServiceFactory serviceFactory) {
        System.out.println("TransactionsController processGetAll request");
        List<TransactionDto> transactionDtoList = serviceFactory.getTransactionService().getTransactionList(request);
        return new PaginatedResponse<>(true, 200, "application/json", transactionDtoList, request.getPage(), request.getSize(),
                transactionDtoList.size(),
                transactionDtoList.size() / request.getSize());
    }

    @Override
    public ControllerResponse<TransactionDto> processUpdate(TransactionRequest request, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public ControllerResponse<TransactionDto> processDelete(long id, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public ControllerResponse<TransactionDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("TransactionsController processGet");
        Optional<TransactionDto> transactionById = serviceFactory.getTransactionService().getTransactionById(id);
        return new ControllerResponse<>(true, 200, "application/json", transactionById.orElse(null));
    }

    @Override
    public ControllerResponse<TransactionDto> processCreate(TransactionRequest request, ServiceFactory serviceFactory) {
        TransactionDto transactionDto = new TransactionDto();
//        if (request.getSenderAccountId() != null) {
            transactionDto.setSenderAccountId(request.getSenderAccountId());
//        }
//        if (request.getRecipientAccountId() != null) {
            transactionDto.setRecipientAccountId(request.getRecipientAccountId());
//        }
//        if (request.getMoney() != null) {
            transactionDto.setMoney(request.getMoney());
//        }
        return new ControllerResponse<>(true, 202, "application/json", serviceFactory.getTransactionService()
                .transferMoney(transactionDto, serviceFactory.getAccountService()));
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public Class<TransactionDto> getDtoClass() {
        return TransactionDto.class;
    }

    @Override
    public Class<TransactionRequest> getRequestClass() {
        return TransactionRequest.class;
    }

    @Override
    public String getPath() {
        return "/transactions";
    }
}
