package my.bankapp.controller;

import my.bankapp.dto.UserDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public class UsersController implements ReadableController<UserDto, UserDto>, CreatableController<UserDto, UserDto>, UpdatableController<UserDto, UserDto>, DeletableController<UserDto, UserDto> {
    @Override
    public ControllerResponse<List<UserDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processGetAll");
//        return new Response(true, serviceFactory.getUserService().getUserById(id));
        return new ControllerResponse<>(true, 200, "application/json", null);
    }

    @Override
    public ControllerResponse<UserDto> processUpdate(UserDto request, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public ControllerResponse<UserDto> processDelete(long id, ServiceFactory serviceFactory) {
        return null;
    }

    @Override
    public ControllerResponse<UserDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processGet");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().getUserById(id));
    }

    @Override
    public ControllerResponse<UserDto> processCreate(UserDto request, ServiceFactory serviceFactory) {
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().createNewUser(request));
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public Class<UserDto> getRequestClass() {
        return UserDto.class;
    }

    @Override
    public String getPath() {
        return "/users";
    }
}
