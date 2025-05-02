package my.bankapp.dao;

import my.bankapp.model.request.GetRequest;

import java.util.stream.Stream;

public interface ReadableDao<MODEL> {

    MODEL findById(long id);

    Stream<MODEL> findAll();

    Stream<MODEL> findAllByParameters(GetRequest request);
}
