package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

public interface CreatableController<DTO, REQ> extends Controller<DTO, REQ> {
    default ControllerResponse<DTO> processCreate(REQ request, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };

    Class<REQ> getCreatableRequestClass();
}
