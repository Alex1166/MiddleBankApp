package my.bankapp.dao;

public interface DeletableDao<MODEL> {

    void delete(long id);
}
