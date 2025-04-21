package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

public interface CreatableController<REQ, RESP> extends Controller<REQ, RESP> {
    default ControllerResponse<RESP> processCreate(REQ request, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };
}
