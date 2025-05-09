package my.bankapp.controller;

import my.bankapp.dto.UserDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.UserRequest;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public class UsersController implements ReadableController<UserDto, UserRequest>, CreatableController<UserDto, UserRequest>, UpdatableController<UserDto, UserRequest>, DeletableController<UserDto, UserRequest> {
    @Override
    public ControllerResponse<List<UserDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processGetAll");
//        return new Response(true, serviceFactory.getUserService().getUserById(id));
        return new ControllerResponse<>(true, 200, "application/json", null);
    }

    @Override
    public ControllerResponse<UserDto> processUpdate(UserRequest request, ServiceFactory serviceFactory) {
        System.out.println("UsersController processUpdate");
        UserDto userDto = serviceFactory.getUserService().getUserById(request.getId());
        if (request.getName() != null) {
            userDto.setName(request.getName());
        }
        if (request.getLogin() != null) {
            userDto.setLogin(request.getLogin());
        }
        if (request.getPassword() != null) {
            userDto.setPassword(request.getPassword());
        } else {
            userDto.setPassword(null);
        }
        System.out.println("userDto = " + userDto);
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().updateUser(userDto));
    }

    @Override
    public ControllerResponse<UserDto> processDelete(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processDelete");
        System.out.println("id = " + id);
        if (serviceFactory.getUserService().deleteUser(id)) {
            return new ControllerResponse<>(true, 204, "application/json", null);
        }
        return new ControllerResponse<>(false, 500, "application/json", null);
    }

    @Override
    public ControllerResponse<UserDto> processGet(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processGet");
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().getUserById(id));
    }

    @Override
    public ControllerResponse<UserDto> processCreate(UserRequest request, ServiceFactory serviceFactory) {
        UserDto userDto = new UserDto();
        if (request.getLogin() != null) {
            userDto.setLogin(request.getLogin());
        }
        if (request.getName() != null) {
            userDto.setName(request.getName());
        }
        if (request.getPassword() != null) {
            userDto.setPassword(request.getPassword());
        }
        return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().createNewUser(userDto));
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public Class<UserDto> getDtoClass() {
        return UserDto.class;
    }

    @Override
    public Class<UserRequest> getRequestClass() {
        return UserRequest.class;
    }

    @Override
    public String getPath() {
        return "/users";
    }
}
