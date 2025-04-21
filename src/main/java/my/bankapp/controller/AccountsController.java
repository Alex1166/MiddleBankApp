package my.bankapp.controller;

import my.bankapp.dto.AccountDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.LoginRequest;
import my.bankapp.model.response.ControllerResponse;
import my.bankapp.model.response.Response;

import java.util.List;

public class AccountsController implements ReadableController<AccountDto, AccountDto>, CreatableController<AccountDto, AccountDto>, UpdatableController<AccountDto, AccountDto>, DeletableController<AccountDto, AccountDto> {
    @Override
    public ControllerResponse<AccountDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGet");

        return new ControllerResponse<>(true, 200, "application/json",  serviceFactory.getAccountService().getAccountById(id));
    }

    @Override
    public ControllerResponse<AccountDto> processCreate(AccountDto request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processCreate");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().createAccount(request));
    }

    @Override
    public ControllerResponse<List<AccountDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGetAll");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().getAccountList(id));
    }

    @Override
    public ControllerResponse<AccountDto> processUpdate(AccountDto request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processUpdate");
        return new ControllerResponse<>(true, 200, "application/json",  serviceFactory.getAccountService().updateAccount(request));
    }

    @Override
    public ControllerResponse<AccountDto> processDelete(long id, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public Class<AccountDto> getRequestClass() {
        return AccountDto.class;
    }

    @Override
    public String getPath() {
        return "/accounts";
    }
}
