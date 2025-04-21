package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

public interface UpdatableController<REQ, RESP> extends Controller<REQ, RESP> {
    default ControllerResponse<RESP> processUpdate(REQ request, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };
}
