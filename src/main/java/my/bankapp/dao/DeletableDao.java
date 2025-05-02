package my.bankapp.dao;

import my.bankapp.model.request.GetRequest;

import java.util.stream.Stream;

public interface DeletableDao<MODEL> {

    boolean delete(long id);
}
