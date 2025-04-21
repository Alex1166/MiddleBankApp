package my.bankapp.factory;

import my.bankapp.controller.AccountsController;
import my.bankapp.controller.Controller;
import my.bankapp.controller.LoginController;
import my.bankapp.controller.TransactionsController;
import my.bankapp.controller.UsersController;

import java.util.ArrayList;
import java.util.List;

public class ControllerFactory {

    private final List<Controller<?, ?>> controllersList = new ArrayList<>();

    public ControllerFactory() {
        controllersList.add(new LoginController());
        controllersList.add(new UsersController());
        controllersList.add(new AccountsController());
        controllersList.add(new TransactionsController());
    }

    public List<Controller<?, ?>> getAllControllers() {
        return controllersList;
    }
}
