package my.bankapp.controller;

public interface Controller<DTO, REQ> {

    String getVersion();

//    Class<DTO> getDtoClass();

//    Class<REQ> getRequestClass();

    String getPath();
}
