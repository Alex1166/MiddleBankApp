package my.bankapp.controller;

import my.bankapp.dto.TransactionCreateDto;
import my.bankapp.dto.TransactionReadDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.request.TransactionRequest;
import my.bankapp.model.response.ControllerResponse;
import my.bankapp.model.response.PaginatedResponse;

import java.util.List;
import java.util.Optional;

public class TransactionsController
        implements ReadableController<TransactionReadDto, TransactionCreateDto>, CreatableController<TransactionReadDto, TransactionCreateDto>,
        UpdatableController<TransactionReadDto, TransactionCreateDto>, DeletableController<TransactionReadDto, TransactionCreateDto> {
    @Override
    public ControllerResponse<List<TransactionReadDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("TransactionsController processGetAll");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getTransactionService().getTransactionList(id));
    }

    @Override
    public PaginatedResponse<List<TransactionReadDto>> processGetAll(GetRequest request, ServiceFactory serviceFactory) {
        System.out.println("TransactionsController processGetAll request");
        List<TransactionReadDto> transactionReadDtoList = serviceFactory.getTransactionService().getTransactionList(request);
        return new PaginatedResponse<>(true, 200, "application/json", transactionReadDtoList, request.getPage(), request.getSize(),
                transactionReadDtoList.size(),
                transactionReadDtoList.size() / request.getSize());
    }

    @Override
    public ControllerResponse<TransactionReadDto> processUpdate(long id, TransactionCreateDto request, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public ControllerResponse<TransactionReadDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("TransactionsController processGet");
        Optional<TransactionReadDto> transactionById = serviceFactory.getTransactionService().getTransactionById(id);
        return new ControllerResponse<>(true, 200, "application/json", transactionById.orElse(null));
    }

    @Override
    public ControllerResponse<TransactionReadDto> processDelete(long id, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public ControllerResponse<TransactionReadDto> processCreate(TransactionCreateDto request, ServiceFactory serviceFactory) {
        TransactionReadDto transactionReadDto = new TransactionReadDto();
//        if (request.getSenderAccountId() != null) {
            transactionReadDto.setSenderAccountId(request.getSenderAccountId());
//        }
//        if (request.getRecipientAccountId() != null) {
            transactionReadDto.setRecipientAccountId(request.getRecipientAccountId());
//        }
//        if (request.getMoney() != null) {
            transactionReadDto.setMoney(request.getMoney());
//        }
        return new ControllerResponse<>(true, 202, "application/json", serviceFactory.getTransactionService()
                .transferMoney(transactionReadDto, serviceFactory.getAccountService()));
    }

    @Override
    public Class<TransactionReadDto> getUpdatableDtoClass() {
        return TransactionReadDto.class;
    }

    @Override
    public Class<TransactionCreateDto> getUpdatableRequestClass() {
        return TransactionCreateDto.class;
    }

    @Override
    public Class<TransactionReadDto> getDeletableDtoClass() {
        return TransactionReadDto.class;
    }

    @Override
    public Class<TransactionReadDto> getReadableDtoClass() {
        return TransactionReadDto.class;
    }

    @Override
    public Class<TransactionCreateDto> getReadableRequestClass() {
        return TransactionCreateDto.class;
    }

    @Override
    public Class<TransactionReadDto> getCreatableDtoClass() {
        return TransactionReadDto.class;
    }

    @Override
    public Class<TransactionCreateDto> getCreatableRequestClass() {
        return TransactionCreateDto.class;
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

//    @Override
//    public Class<TransactionDto> getDtoClass() {
//        return TransactionDto.class;
//    }
//
//    @Override
//    public Class<TransactionRequest> getRequestClass() {
//        return TransactionRequest.class;
//    }

    @Override
    public String getPath() {
        return "/transactions";
    }
}
