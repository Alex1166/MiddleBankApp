package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.IdRequest;
import my.bankapp.model.response.ControllerResponse;

public interface UpdatableController<DTO, REQ extends IdRequest> extends Controller<DTO, REQ> {
    default ControllerResponse<DTO> processUpdate(REQ request, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };
}
