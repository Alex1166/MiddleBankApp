package my.bankapp.controller;

import my.bankapp.dto.UserCreateDto;
import my.bankapp.dto.UserReadDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public class UsersController implements ReadableController<UserReadDto, UserCreateDto>, CreatableController<UserReadDto, UserCreateDto>,
        UpdatableController<UserReadDto, UserCreateDto>, DeletableController<UserReadDto, UserCreateDto> {
    @Override
    public ControllerResponse<List<UserReadDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processGetAll");
        return new ControllerResponse<>(true, 200, "application/json", null);
    }

    @Override
    public ControllerResponse<UserReadDto> processUpdate(long id, UserCreateDto request, ServiceFactory serviceFactory) {
        System.out.println("UsersController processUpdate");

        serviceFactory.getUserService().updateUser(id, request);
        return new ControllerResponse<>(true, 200, "application/json", null);
    }

    @Override
    public ControllerResponse<UserReadDto> processDelete(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processDelete");
        System.out.println("id = " + id);
        if (serviceFactory.getUserService().deleteUser(id)) {
            return new ControllerResponse<>(true, 204, "application/json", null);
        }
        return new ControllerResponse<>(false, 500, "application/json", null);
    }

    @Override
    public ControllerResponse<UserReadDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processGet");

        if (serviceFactory.getUserService().getUserById(id).isPresent()) {
            return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().getUserById(id).get());
        } else {
            return new ControllerResponse<>(true, 404, "application/json", null);
        }

    }

    @Override
    public ControllerResponse<UserReadDto> processCreate(UserCreateDto request, ServiceFactory serviceFactory) {
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().createNewUser(request));
    }

    @Override
    public Class<UserCreateDto> getCreatableRequestClass() {
        return UserCreateDto.class;
    }

    @Override
    public Class<UserReadDto> getReadableDtoClass() {
        return UserReadDto.class;
    }

    @Override
    public Class<UserCreateDto> getUpdatableRequestClass() {
        return UserCreateDto.class;
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getPath() {
        return "/users";
    }
}
