package my.bankapp.controller;

import my.bankapp.dto.AccountDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.AccountRequest;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.response.ControllerResponse;
import my.bankapp.model.response.PaginatedResponse;

import java.util.List;

public class AccountsController implements ReadableController<AccountDto, AccountRequest>, CreatableController<AccountDto, AccountRequest>,
        UpdatableController<AccountDto, AccountRequest>, DeletableController<AccountDto, AccountRequest> {
    @Override
    public ControllerResponse<AccountDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGet");

        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().getAccountById(id));
    }

    @Override
    public ControllerResponse<AccountDto> processCreate(AccountRequest request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processCreate");
        AccountDto accountDto = new AccountDto();
        if (request.getUserId() != null) {
            accountDto.setUserId(request.getUserId());
        }
        if (request.getType() != null) {
            accountDto.setType(request.getType());
        }
        if (request.getTitle() != null) {
            accountDto.setTitle(request.getTitle());
        }
        System.out.println("accountDto = " + accountDto);
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().createAccount(accountDto));
    }

    @Override
    public ControllerResponse<List<AccountDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGetAll");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().getAccountList(id));
    }

    @Override
    public PaginatedResponse<List<AccountDto>> processGetAll(GetRequest request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGetAll request");
        List<AccountDto> accountList = serviceFactory.getAccountService().getAccountList(request);
        return new PaginatedResponse<>(true, 200, "application/json", accountList, request.getPage(), request.getSize(), accountList.size(),
                accountList.size() / request.getSize());
//        return new PaginatedResponse<>(true, 200, "application/json", serviceFactory.getAccountService().getAccountList(
//                (long) request.getFilterBy().get("id")));
    }

    ;

    @Override
    public ControllerResponse<AccountDto> processUpdate(AccountRequest request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processUpdate");
        AccountDto accountDto = serviceFactory.getAccountService().getAccountById(request.getId());
//        AccountDto accountDto = new AccountDto();
        if (request.getIsDefault() != null) {
            accountDto.setDefault(request.getIsDefault());
        }
        if (request.getType() != null) {
            accountDto.setType(request.getType());
        }
        if (request.getTitle() != null) {
            accountDto.setTitle(request.getTitle());
        }
        System.out.println("accountDto = " + accountDto);
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().updateAccount(accountDto));
    }

    @Override
    public ControllerResponse<AccountDto> processDelete(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processDelete");
        System.out.println("id = " + id);
        if (serviceFactory.getAccountService().deleteAccount(id)) {
            return new ControllerResponse<>(true, 204, "application/json", null);
        }
        return new ControllerResponse<>(false, 500, "application/json", null);
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public Class<AccountRequest> getRequestClass() {
        return AccountRequest.class;
    }

    @Override
    public Class<AccountDto> getDtoClass() {
        return AccountDto.class;
    }

    @Override
    public String getPath() {
        return "/accounts";
    }
}
