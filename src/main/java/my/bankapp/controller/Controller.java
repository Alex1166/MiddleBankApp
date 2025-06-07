package my.bankapp.controller;

import my.bankapp.model.request.IdRequest;

public interface Controller<DTO, REQ> {

    String getVersion();

//    Class<DTO> getDtoClass();

//    Class<REQ> getRequestClass();

    String getPath();
}
