package my.bankapp.dao;

import java.sql.Connection;

public interface CreatableDao<MODEL> {

    MODEL insert(MODEL model);

    MODEL insert(MODEL model, Connection connection);
}
