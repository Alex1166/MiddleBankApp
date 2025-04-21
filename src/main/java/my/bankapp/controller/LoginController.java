package my.bankapp.controller;

import my.bankapp.dto.UserDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.LoginRequest;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public class LoginController implements CreatableController<LoginRequest, UserDto>, AuthenticatingController {
    @Override
    public ControllerResponse<UserDto> processCreate(LoginRequest request, ServiceFactory serviceFactory) {

//        response.setSuccess(request.getUsername().equals("alex") && request.getPassword().equals("123"));


        if (serviceFactory.getUserService().isPasswordCorrect(request.getUsername(), request.getPassword())) {
            UserDto user = serviceFactory.getUserService().getUserByLogin(request.getUsername());

            return new ControllerResponse<>(true, 200, "application/json", user);
        } else {
//            return new Response(false, null);
            return new ControllerResponse<>(false, 200, "application/json", null);

        }
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public Class<LoginRequest> getRequestClass() {
        return LoginRequest.class;
    }

    @Override
    public String getPath() {
        return "/login";
    }
}
