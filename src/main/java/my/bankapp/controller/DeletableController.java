package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.IdRequest;
import my.bankapp.model.response.ControllerResponse;

public interface DeletableController<DTO, REQ> extends Controller<DTO, REQ> {
    default ControllerResponse<DTO> processDelete(long id, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };
}
