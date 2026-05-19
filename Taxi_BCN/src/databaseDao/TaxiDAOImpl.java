package databaseDao;

import project_models.*;
import project_models.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaxiDAOImpl implements TaxiDAO {

    private final Connection connection;

    // Base SQL with JOIN to fetch driver details
    private static final String SELECT_BASE = """
        SELECT t.*, 
               c.first_name    AS c_first_name,
               c.last_name     AS c_last_name,
               c.age           AS c_age,
               c.national_id   AS c_national_id,
               c.taxi_license  AS c_taxi_license
        FROM taxis t
        LEFT JOIN drivers c ON t.driver_id = c.id
    """;

    public TaxiDAOImpl(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────────────────────

    @Override
    public int insert(Taxi taxi) throws SQLException {
        int driverId = -1;
        if (taxi.getDriver() != null) {
            DriverDAOImpl driverDAO = new DriverDAOImpl(connection);
            driverId = driverDAO.insert(taxi.getDriver());
        }

        String sql = """
            INSERT OR IGNORE INTO taxis (license_plate, color, capacity, driver_id, row_pos, col_pos, type, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, taxi.getLicensePlate());
            ps.setString(2, taxi.getColor());
            ps.setInt   (3, taxi.getCapacity());
            if (driverId > 0) ps.setInt(4, driverId);
            else              ps.setNull(4, Types.INTEGER);
            ps.setInt   (5, taxi.getPosition().getRow());
            ps.setInt   (6, taxi.getPosition().getColumn());
            ps.setString(7, taxi.getType().name());
            ps.setString(8, taxi.getStatus().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return findIdByLicensePlate(taxi.getLicensePlate());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SELECT
    // ─────────────────────────────────────────────────────────────

    @Override
    public Optional<Taxi> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE t.id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Taxi> findByLicensePlate(String licensePlate) throws SQLException {
        String sql = SELECT_BASE + " WHERE t.license_plate = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, licensePlate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<Taxi> findAll() throws SQLException {
        List<Taxi> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(SELECT_BASE)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Taxi> findByStatus(TaxiStatus status) throws SQLException {
        List<Taxi> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Taxi> findByType(TaxiType type) throws SQLException {
        List<Taxi> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.type = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Taxi> findAvailableByType(TaxiType type) throws SQLException {
        List<Taxi> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.type = ? AND t.status = 'AVAILABLE'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean update(Taxi taxi) throws SQLException {
        int driverId = -1;
        if (taxi.getDriver() != null) {
            DriverDAOImpl driverDAO = new DriverDAOImpl(connection);
            driverId = driverDAO.insert(taxi.getDriver());
        }

        String sql = """
            UPDATE taxis
            SET color = ?, capacity = ?, driver_id = ?,
                row_pos = ?, col_pos = ?, type = ?, status = ?
            WHERE license_plate = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, taxi.getColor());
            ps.setInt   (2, taxi.getCapacity());
            if (driverId > 0) ps.setInt(3, driverId);
            else              ps.setNull(3, Types.INTEGER);
            ps.setInt   (4, taxi.getPosition().getRow());
            ps.setInt   (5, taxi.getPosition().getColumn());
            ps.setString(6, taxi.getType().name());
            ps.setString(7, taxi.getStatus().name());
            ps.setString(8, taxi.getLicensePlate());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int id, TaxiStatus status) throws SQLException {
        String sql = "UPDATE taxis SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM taxis WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────────────────────────────

    private Taxi mapRow(ResultSet rs) throws SQLException {
        // Rebuild driver if present
        Driver driver = null;
        String dFirstName = rs.getString("c_first_name");
        if (dFirstName != null) {
            driver = new Driver(
                dFirstName,
                rs.getString("c_last_name"),
                rs.getInt   ("c_age"),
                rs.getString("c_national_id"),
                rs.getString("c_taxi_license")
            );
        }

        Position position = new Position(rs.getInt("row_pos"), rs.getInt("col_pos"));

        Taxi taxi = new Taxi(
            rs.getString("license_plate"),
            rs.getString("color"),
            rs.getInt   ("capacity"),
            driver,
            position,
            TaxiType.valueOf(rs.getString("type"))
        );
        taxi.setStatus(TaxiStatus.valueOf(rs.getString("status")));
        return taxi;
    }

    private int findIdByLicensePlate(String licensePlate) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM taxis WHERE license_plate = ?")) {
            ps.setString(1, licensePlate);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
}
