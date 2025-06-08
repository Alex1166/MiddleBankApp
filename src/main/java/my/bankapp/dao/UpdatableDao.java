package my.bankapp.dao;

import my.bankapp.model.request.GetRequest;

import java.sql.Connection;
import java.util.stream.Stream;

public interface UpdatableDao<MODEL> {

    void update(MODEL model);

    void update(MODEL model, Connection connection);
}
