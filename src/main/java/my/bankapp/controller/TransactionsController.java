package my.bankapp.controller;

import my.bankapp.dto.TransactionDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.TransactionRequest;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public class TransactionsController implements ReadableController<TransactionDto, TransactionRequest>, CreatableController<TransactionDto, TransactionRequest>, UpdatableController<TransactionDto, TransactionRequest>, DeletableController<TransactionDto, TransactionRequest> {
    @Override
    public ControllerResponse<List<TransactionDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        return null;
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
        return null;
    }

    @Override
    public ControllerResponse<TransactionDto> processCreate(TransactionRequest request, ServiceFactory serviceFactory) {
        TransactionDto transactionDto = new TransactionDto();
        if (request.getSenderAccountId() != null) {
            transactionDto.setSenderAccountId(request.getSenderAccountId());
        }
        if (request.getRecipientAccountId() != null) {
            transactionDto.setRecipientAccountId(request.getRecipientAccountId());
        }
        if (request.getMoney() != null) {
            transactionDto.setMoney(request.getMoney());
        }
        return new ControllerResponse<>(true, 202, "application/json", serviceFactory.getTransactionService().transferMoney(transactionDto));
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
