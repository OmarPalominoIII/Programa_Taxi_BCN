package dao;

import models.Customer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public interface CustomerDAO {
    int createCustomer(Customer customer)                       throws SQLException;
    Optional<Customer> findByNationalId(String identity) throws SQLException;
    Optional<Customer> findCustomerById(int id)                           throws SQLException;
    ArrayList<Customer> findAllCustomers()                            throws SQLException;
    boolean update(Customer customer)                   throws SQLException;
    boolean delete(int id)                              throws SQLException;
}
