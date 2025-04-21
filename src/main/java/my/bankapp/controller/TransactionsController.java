package my.bankapp.controller;

import my.bankapp.dto.TransactionDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public class TransactionsController implements ReadableController<TransactionDto, TransactionDto>, CreatableController<TransactionDto, TransactionDto>, UpdatableController<TransactionDto, TransactionDto>, DeletableController<TransactionDto, TransactionDto> {
    @Override
    public ControllerResponse<List<TransactionDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public ControllerResponse<TransactionDto> processUpdate(TransactionDto request, ServiceFactory serviceFactory) {
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
    public ControllerResponse<TransactionDto> processCreate(TransactionDto request, ServiceFactory serviceFactory) {
        return new ControllerResponse<>(true, 202, "application/json", serviceFactory.getTransactionService().transferMoney(request));
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public Class<TransactionDto> getRequestClass() {
        return TransactionDto.class;
    }

    @Override
    public String getPath() {
        return "/transactions";
    }
}
