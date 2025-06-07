package my.bankapp.controller;

import my.bankapp.dto.UserDto;
import my.bankapp.dto.UserReadDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.LoginRequest;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;
import java.util.Optional;

public class LoginController implements CreatableController<UserReadDto, LoginRequest>, AuthenticatingController {
    @Override
    public ControllerResponse<UserReadDto> processCreate(LoginRequest request, ServiceFactory serviceFactory) {

//        response.setSuccess(request.getUsername().equals("alex") && request.getPassword().equals("123"));


        if (serviceFactory.getUserService().isPasswordCorrect(request.getLogin(), request.getPassword())) {
            if (serviceFactory.getUserService().getUserByLogin(request.getLogin()).isPresent()) {
                return new ControllerResponse<>(true, 200, "application/json", serviceFactory.getUserService().getUserByLogin(request.getLogin()).get());
            } else {
                return new ControllerResponse<>(true, 404, "application/json", null);
            }
        } else {
            return new ControllerResponse<>(true, 401, "application/json", null);

        }
    }

    @Override
    public Class<UserReadDto> getCreatableDtoClass() {
        return UserReadDto.class;
    }

    @Override
    public Class<LoginRequest> getCreatableRequestClass() {
        return LoginRequest.class;
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getPath() {
        return "/login";
    }
}
