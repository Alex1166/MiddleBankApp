package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.request.IdRequest;
import my.bankapp.model.response.ControllerResponse;
import my.bankapp.model.response.PaginatedResponse;

import java.util.List;

public interface ReadableController<DTO, REQ extends IdRequest> extends Controller<DTO, REQ> {
    default ControllerResponse<List<DTO>> processGetAll(long id, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };

    default PaginatedResponse<List<DTO>> processGetAll(GetRequest request, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };

    default ControllerResponse<DTO> processGet(long id, ServiceFactory serviceFactory) {
        throw new RuntimeException("Method not supported");
    };
}
