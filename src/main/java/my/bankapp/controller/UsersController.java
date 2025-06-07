package my.bankapp.controller;

import my.bankapp.dto.IdDto;
import my.bankapp.dto.UserCreateDto;
import my.bankapp.dto.UserDto;
import my.bankapp.dto.UserReadDto;
import my.bankapp.exception.IdentifierNotProvidedException;
import my.bankapp.exception.UserNotFoundException;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.UserRequest;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public class UsersController implements ReadableController<UserReadDto, UserCreateDto>, CreatableController<UserReadDto, UserCreateDto>,
        UpdatableController<UserReadDto, UserCreateDto>, DeletableController<UserReadDto, UserCreateDto> {
    @Override
    public ControllerResponse<List<UserReadDto>> processGetAll(long id, ServiceFactory serviceFactory) {
        System.out.println("UsersController processGetAll");
//        return new Response(true, serviceFactory.getUserService().getUserById(id));
        return new ControllerResponse<>(true, 200, "application/json", null);
    }

    @Override
    public ControllerResponse<UserReadDto> processUpdate(long id, UserCreateDto request, ServiceFactory serviceFactory) {
        System.out.println("UsersController processUpdate");

//        if (request.getId() == null) {
//            throw new IdentifierNotProvidedException("Id of user to update was not provided");
//        }

        UserReadDto userReadDto = serviceFactory.getUserService().getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id %s not found".formatted(id)));

        UserDto userDto = new UserDto();

        userDto.setId(id);
        userDto.setIsDeleted(false);
        if (request.getName() != null) {
            userDto.setName(request.getName());
        } else {
            userDto.setName(userReadDto.getName());
        }
        if (request.getLogin() != null) {
            userDto.setLogin(request.getLogin());
        } else {
            userDto.setLogin(userReadDto.getLogin());
        }
        if (request.getPassword() != null) {
            userDto.setPassword(request.getPassword());
        }
        System.out.println("userDto = " + userDto);
        serviceFactory.getUserService().updateUser(userDto);

        userReadDto = new UserReadDto(userDto.getId(), userDto.getLogin(), userDto.getName(), userDto.getIsDeleted());

        return new ControllerResponse<>(true, 200, "application/json", userReadDto);
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
    public Class<UserReadDto> getCreatableDtoClass() {
        return UserReadDto.class;
    }

    @Override
    public Class<UserCreateDto> getCreatableRequestClass() {
        return UserCreateDto.class;
    }

    @Override
    public Class<UserReadDto> getDeletableDtoClass() {
        return UserReadDto.class;
    }

    @Override
    public Class<UserReadDto> getReadableDtoClass() {
        return UserReadDto.class;
    }

    @Override
    public Class<UserCreateDto> getReadableRequestClass() {
        return UserCreateDto.class;
    }

    @Override
    public Class<UserReadDto> getUpdatableDtoClass() {
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
