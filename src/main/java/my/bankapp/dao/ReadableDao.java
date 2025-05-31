package my.bankapp.dao;

import my.bankapp.model.request.GetRequest;

import java.util.Optional;
import java.util.stream.Stream;

public interface ReadableDao<MODEL> {

    Optional<MODEL> findById(long id);

    Stream<MODEL> findAll();

    Stream<MODEL> findAllByParameters(GetRequest request);
}
