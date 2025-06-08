package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.IdRequest;
import my.bankapp.model.response.ControllerResponse;

public interface UpdatableController<DTO, REQ> extends Controller<DTO, REQ> {
    default ControllerResponse<DTO> processUpdate(long id, REQ request, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };

    Class<REQ> getUpdatableRequestClass();
}
