package databaseDao;

import project_models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {

    private final Connection connection;

    public CustomerDAOImpl(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────────────────────

    @Override
    public int insert(Customer customer) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO customers (first_name, last_name, age, national_id, phone_number)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    // ─────────────────────────────────────────────────────────────
    // SELECT
    // ─────────────────────────────────────────────────────────────

    @Override
    public Optional<Customer> findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByNationalId(String identity) throws SQLException {
        String sql = "SELECT * FROM customers WHERE national_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean update(Customer customer) throws SQLException {
        String sql = """
            UPDATE customers
            SET first_name = ?, last_name = ?, age = ?, phone_number = ?
            WHERE national_id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setInt   (3, customer.getAge());
            ps.setString(4, customer.getPhoneNumber());
            ps.setString(5, customer.getNationalId());
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────────────────────────────

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getInt   ("age"),
            rs.getString("national_id"),
            rs.getString("phone_number")
        );
    }

    private int findIdByNationalId(String identity) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM customers WHERE national_id = ?")) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
}
