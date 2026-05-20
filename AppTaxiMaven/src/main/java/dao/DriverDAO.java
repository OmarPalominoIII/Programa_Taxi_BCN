package dao;

import models.Driver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public interface DriverDAO {
    int createDriver(Driver driver)                           throws SQLException;
    Optional<Driver> findDriverById(int id)                           throws SQLException;
    Optional<Driver> findByNationalId(String identity) throws SQLException;
    ArrayList<Driver> findAllDrivers()                      throws SQLException;
    boolean update(Driver driver)                           throws SQLException;
    boolean delete(int id)                                  throws SQLException;
}
