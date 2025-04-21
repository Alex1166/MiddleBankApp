package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;

import java.util.List;

public interface ReadableController<REQ, RESP> extends Controller<REQ, RESP> {
    default ControllerResponse<List<RESP>> processGetAll(long id, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };

    default ControllerResponse<RESP> processGet(long id, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };
}
