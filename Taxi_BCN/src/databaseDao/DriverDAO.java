package databaseDao;

import project_models.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DriverDAO {
    int insert(Driver driver)                       throws SQLException;
    Optional<Driver> findById(int id)               throws SQLException;
    Optional<Driver> findByNationalId(String identity) throws SQLException;
    List<Driver> findAll()                          throws SQLException;
    boolean update(Driver driver)                   throws SQLException;
    boolean delete(int id)                          throws SQLException;
}
