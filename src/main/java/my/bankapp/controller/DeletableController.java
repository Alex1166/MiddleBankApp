package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

public interface DeletableController<REQ, RESP> extends Controller<REQ, RESP> {
    default ControllerResponse<RESP> processDelete(long id, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };
}
