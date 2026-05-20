package dao.impl;

import dao.TaxiDAO;
import models.Position;
import models.Taxi;
import models.TaxiStatus;
import models.TaxiType;
import models.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class TaxiImplDAO implements TaxiDAO {

    private final Connection conn;

    public TaxiImplDAO(Connection conn) {
        this.conn = conn;
    }

    // Basic sql JOIN to fetch driver details
    private static final String SELECT_BASE = """
        SELECT t.*, 
               c.Firstname      AS c_firstname,
               c.Lastname       AS c_lastname,
               c.Age            AS c_age,
               c.DNI            AS c_dni,
               c.Driver_license AS c_driverlicense
        FROM Taxi t
        LEFT JOIN Driver c ON t.ID_Driver = c.ID_Driver
    """;

    // Private methods
    private Taxi mapRow(ResultSet rs) throws SQLException {
        // Rebuild driver if present
        Driver driver = null;
        String dFirstName = rs.getString("c_firstname");

        if (dFirstName != null) {
            driver = new Driver();
            driver.setFirstName(dFirstName);
            driver.setLastName(rs.getString("c_lastname"));
            driver.setAge(rs.getInt("c_age"));
            driver.setNationalId(rs.getString("c_dni"));
            driver.setTaxiLicense(rs.getString("c_driverlicense"));
        }

        Position position = new Position();
        position.setLatitude(rs.getDouble("Latitude"));
        position.setLongitude(rs.getDouble("Longitude"));

        Taxi taxi = new Taxi();
        taxi.setIdTaxi(rs.getInt("ID_Taxi"));
        taxi.setLicensePlate(rs.getString("Car_license"));
        taxi.setColor(rs.getString("Color"));
        taxi.setCapacity(rs.getInt("Capacity"));
        taxi.setDriver(driver);
        taxi.setPosition(position);
        taxi.setType(TaxiType.valueOf(rs.getString("Type")));
        taxi.setStatus(TaxiStatus.valueOf(rs.getString("State")));
        return taxi;
    }

    private int findIdByLicensePlate(String licensePlate) throws SQLException {

        String sql = """
                SELECT ID_Taxi FROM Taxi WHERE Car_license = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, licensePlate);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("ID_Taxi") : -1;
        }
    }

    @Override
    public int createTaxi(Taxi taxi) throws SQLException {
        int driverId = -1;
        if (taxi.getDriver() != null) {
            DriverImplDAO driverDAO = new DriverImplDAO(conn);
            driverId = driverDAO.createDriver(taxi.getDriver());
        }

        String sql = """
            INSERT OR IGNORE INTO Taxi (Car_license, Color, Capacity, ID_Driver, Longitude, Latitude, Type, State)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, taxi.getLicensePlate());
            ps.setString(2, taxi.getColor());
            ps.setInt   (3, taxi.getCapacity());

            if (driverId > 0) {
                ps.setInt(4, driverId);
            }
            else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setDouble(5, taxi.getPosition().getLongitude());
            ps.setDouble   (6, taxi.getPosition().getLatitude());
            ps.setString(7, taxi.getType().name());
            ps.setString(8, taxi.getStatus().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return findIdByLicensePlate(taxi.getLicensePlate());
        }
    }

    @Override
    public Optional<Taxi> findTaxiById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE t.ID_Taxi = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Taxi> findByLicensePlate(String licensePlate) throws SQLException {
        String sql = SELECT_BASE + " WHERE t.Car_license = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, licensePlate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<Taxi> findAllTaxis() throws SQLException {
        ArrayList<Taxi> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(SELECT_BASE)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public ArrayList<Taxi> findByStatus(TaxiStatus status) throws SQLException {
        ArrayList<Taxi> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.State = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public ArrayList<Taxi> findByType(TaxiType type) throws SQLException {
        ArrayList<Taxi> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.Type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public ArrayList<Taxi> findAvailableByType(TaxiType type) throws SQLException {
        ArrayList<Taxi> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.Type = ? AND t.State = 'AVAILABLE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean update(Taxi taxi) throws SQLException {
        int driverId = -1;
        if (taxi.getDriver() != null) {
            DriverImplDAO driverDAO = new DriverImplDAO(conn);
            driverId = driverDAO.createDriver(taxi.getDriver());
        }

        String sql = """
            UPDATE Taxi
            SET Color = ?, Capacity = ?, ID_Driver = ?,
                Longitude = ?, Latitude = ?, Type = ?, State = ?
            WHERE Car_license = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, taxi.getColor());
            ps.setInt   (2, taxi.getCapacity());
            if (driverId > 0) ps.setInt(3, driverId);
            else              ps.setNull(3, Types.INTEGER);
            ps.setDouble   (4, taxi.getPosition().getLongitude());
            ps.setDouble   (5, taxi.getPosition().getLatitude());
            ps.setString(6, taxi.getType().name());
            ps.setString(7, taxi.getStatus().name());
            ps.setString(8, taxi.getLicensePlate());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePosition(int id, Position position) throws SQLException {
        String sql = "UPDATE Taxi SET Longitude = ?, Latitude = ? WHERE ID_Taxi = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, position.getLongitude());
            ps.setDouble(2, position.getLatitude());
            ps.setInt   (3, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int id, TaxiStatus status) throws SQLException {
        String sql = "UPDATE Taxi SET State = ? WHERE ID_Taxi = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        }
    }
}
