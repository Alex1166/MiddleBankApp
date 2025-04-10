package my.bankapp.dao;

import java.util.List;
import java.util.stream.Stream;

public interface GenericDao<MODEL> {

    MODEL findById(long id);

    Stream<MODEL> findAll();

    MODEL insert(MODEL model);

    MODEL update(MODEL model);

    boolean delete(long id);
}
