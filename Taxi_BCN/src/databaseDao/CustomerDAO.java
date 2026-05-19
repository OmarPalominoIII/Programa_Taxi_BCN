package databaseDao;

import project_models.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    int insert(Customer customer)                       throws SQLException;
    Optional<Customer> findById(int id)                 throws SQLException;
    Optional<Customer> findByNationalId(String identity) throws SQLException;
    List<Customer> findAll()                            throws SQLException;
    boolean update(Customer customer)                   throws SQLException;
    boolean delete(int id)                              throws SQLException;
}
