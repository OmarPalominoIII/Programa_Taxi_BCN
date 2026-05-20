package dao.impl;

import dao.CustomerDAO;
import models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class CustomerImplDAO implements CustomerDAO {

    private final Connection conn;

    public CustomerImplDAO(Connection conn) {
        this.conn = conn;
    }


    // private methods
    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("Firstname"),
                rs.getString("Lastname"),
                rs.getInt   ("Age"),
                rs.getString("DNI"),
                rs.getString("Phone")
        );
    }

    private int findIdByNationalId(String identity) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ID_Customer FROM Customer WHERE DNI = ?")) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("ID_Customer") : -1;
        }
    }

    @Override
    public int createCustomer(Customer customer) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO Customer (Firstname, Lastname, Age, DNI, Phone)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setInt   (3, customer.getAge());
            ps.setString(4, customer.getNationalId());
            ps.setString(5, customer.getPhoneNumber());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

            return findIdByNationalId(customer.getNationalId());
        }
    }

    @Override
    public Optional<Customer> findByNationalId(String identity) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE DNI = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findCustomerById(int id) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE ID_Customer = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<Customer> findAllCustomers() throws SQLException {
        ArrayList<Customer> list = new ArrayList<>();

        String sql = "SELECT * FROM Customer";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs   = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean update(Customer customer) throws SQLException {
        String sql = """
            UPDATE Customer
            SET Firstname = ?, Lastname = ?, Age = ?, Phone = ?
            WHERE DNI = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setInt   (3, customer.getAge());
            ps.setString(4, customer.getPhoneNumber());
            ps.setString(5, customer.getNationalId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Customer WHERE ID_Customer = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
