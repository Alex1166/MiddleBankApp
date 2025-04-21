package my.bankapp.controller;

import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.LoginRequest;
import my.bankapp.model.response.ControllerResponse;
import my.bankapp.model.response.Response;

import java.util.List;

public interface Controller<REQ, RESP> {

    String getVersion();

    Class<REQ> getRequestClass();

    String getPath();
}
