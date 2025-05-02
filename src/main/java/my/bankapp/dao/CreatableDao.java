package my.bankapp.dao;

import java.util.stream.Stream;

public interface CreatableDao<MODEL> {

    MODEL insert(MODEL model);
}
