package my.bankapp.controller;

import my.bankapp.dto.AccountCreateDto;
import my.bankapp.dto.AccountReadDto;
import my.bankapp.exception.AccountNotFoundException;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.response.ControllerResponse;
import my.bankapp.model.response.PaginatedResponse;

import java.util.List;

public class AccountsController implements ReadableController<AccountReadDto, AccountCreateDto>, CreatableController<AccountReadDto, AccountCreateDto>,
        UpdatableController<AccountReadDto, AccountCreateDto>, DeletableController<AccountReadDto, AccountCreateDto> {
    @Override
    public ControllerResponse<AccountReadDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGet");

        if (serviceFactory.getAccountService().getAccountById(id).isPresent()) {
            return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().getAccountById(id).get());
        } else {
            return new ControllerResponse<>(true, 404, "application/json", null);
        }

    }

    @Override
    public ControllerResponse<AccountReadDto> processCreate(AccountCreateDto request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processCreate");
        AccountReadDto accountDto = new AccountReadDto();
//        if (request.getUserId() != null) {
        accountDto.setUserId(request.getUserId());
//        }
//        if (request.getType() != null) {
        accountDto.setType(request.getType());
//        }
//        if (request.getTitle() != null) {
        accountDto.setTitle(request.getTitle());
//        }
        System.out.println("accountDto = " + accountDto);
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().createAccount(accountDto));
    }

    @Override
    public ControllerResponse<List<AccountReadDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGetAll");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getAccountService().getAccountList(id));
    }

    @Override
    public PaginatedResponse<List<AccountReadDto>> processGetAll(GetRequest request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processGetAll request");
        List<AccountReadDto> accountList = serviceFactory.getAccountService().getAccountList(request);
        return new PaginatedResponse<>(true, 200, "application/json", accountList, request.getPage(), request.getSize(), accountList.size(),
                accountList.size() / request.getSize());
    }

    ;

    @Override
    public ControllerResponse<AccountReadDto> processUpdate(long id, AccountCreateDto request, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processUpdate");

//        if (request.getId() == null) {
//            throw new IdentifierNotProvidedException("Id of account to update was not provided");
//        }

        AccountReadDto accountDto = serviceFactory.getAccountService().getAccountById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account with id %s not found".formatted(id)));
//        AccountDto accountDto = new AccountDto();

        accountDto.setId(id);
        accountDto.setIsDeleted(false);
        if (request.getIsDefault() != null) {
            accountDto.setIsDefault(request.getIsDefault());
        }
        if (request.getType() != null) {
            accountDto.setType(request.getType());
        }
        if (request.getTitle() != null) {
            accountDto.setTitle(request.getTitle());
        }
        System.out.println("accountDto = " + accountDto);
        serviceFactory.getAccountService().updateAccount(accountDto);
        return new ControllerResponse<>(true, 200, "application/json", accountDto);
    }

    @Override
    public ControllerResponse<AccountReadDto> processDelete(long id, ServiceFactory serviceFactory) {
        System.out.println("AccountsController processDelete");
        System.out.println("id = " + id);
        if (serviceFactory.getAccountService().deleteAccount(id)) {
            return new ControllerResponse<>(true, 204, "application/json", null);
        }
        return new ControllerResponse<>(false, 500, "application/json", null);
    }

    @Override
    public Class<AccountReadDto> getCreatableDtoClass() {
        return AccountReadDto.class;
    }

    @Override
    public Class<AccountCreateDto> getCreatableRequestClass() {
        return AccountCreateDto.class;
    }

    @Override
    public Class<AccountReadDto> getDeletableDtoClass() {
        return AccountReadDto.class;
    }

    @Override
    public Class<AccountReadDto> getReadableDtoClass() {
        return AccountReadDto.class;
    }

    @Override
    public Class<AccountCreateDto> getReadableRequestClass() {
        return AccountCreateDto.class;
    }

    @Override
    public Class<AccountReadDto> getUpdatableDtoClass() {
        return AccountReadDto.class;
    }

    @Override
    public Class<AccountCreateDto> getUpdatableRequestClass() {
        return AccountCreateDto.class;
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getPath() {
        return "/accounts";
    }
}
