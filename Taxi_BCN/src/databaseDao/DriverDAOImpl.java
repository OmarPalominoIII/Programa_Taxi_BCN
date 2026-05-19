package databaseDao;

import project_models.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriverDAOImpl implements DriverDAO {

    private final Connection connection;

    public DriverDAOImpl(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────────────────────

    @Override
    public int insert(Driver driver) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO drivers (first_name, last_name, age, national_id, taxi_license)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, driver.getFirstName());
            ps.setString(2, driver.getLastName());
            ps.setInt   (3, driver.getAge());
            ps.setString(4, driver.getNationalId());
            ps.setString(5, driver.getTaxiLicense());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

            // Already existed: return its id
            return findIdByNationalId(driver.getNationalId());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SELECT
    // ─────────────────────────────────────────────────────────────

    @Override
    public Optional<Driver> findById(int id) throws SQLException {
        String sql = "SELECT * FROM drivers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Driver> findByNationalId(String identity) throws SQLException {
        String sql = "SELECT * FROM drivers WHERE national_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<Driver> findAll() throws SQLException {
        List<Driver> list = new ArrayList<>();
        String sql = "SELECT * FROM drivers";
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
    public boolean update(Driver driver) throws SQLException {
        String sql = """
            UPDATE drivers
            SET first_name = ?, last_name = ?, age = ?, taxi_license = ?
            WHERE national_id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, driver.getFirstName());
            ps.setString(2, driver.getLastName());
            ps.setInt   (3, driver.getAge());
            ps.setString(4, driver.getTaxiLicense());
            ps.setString(5, driver.getNationalId());
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM drivers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────────────────────────────

    private Driver mapRow(ResultSet rs) throws SQLException {
        return new Driver(
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getInt   ("age"),
            rs.getString("national_id"),
            rs.getString("taxi_license")
        );
    }

    private int findIdByNationalId(String identity) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM drivers WHERE national_id = ?")) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
}
