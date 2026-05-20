package dao.impl;

import dao.DriverDAO;
import models.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class DriverImplDAO implements DriverDAO {
    private final Connection conn;

    public DriverImplDAO(Connection conn){
        this.conn = conn;
    }

    // Private methods

    private Driver mapRow(ResultSet rs) throws SQLException {
        return new Driver(
                rs.getString("Firstname"),
                rs.getString("Lastname"),
                rs.getInt   ("Age"),
                rs.getString("DNI"),
                rs.getString("Driver_license")
        );
    }

    private int findIdByNationalId(String identity) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ID_Driver FROM Driver WHERE DNI = ?")) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("ID_Driver") : -1;
        }
    }


    @Override
    public int createDriver(Driver driver) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO Driver (Firstname, Lastname, Age, DNI, Driver_license)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    @Override
    public Optional<Driver> findDriverById(int id) throws SQLException {
        String sql = "SELECT * FROM Driver WHERE ID_Driver = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Driver> findByNationalId(String identity) throws SQLException {
        String sql = "SELECT * FROM Driver WHERE DNI = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<Driver> findAllDrivers() throws SQLException {
        ArrayList<Driver> list = new ArrayList<>();
        String sql = "SELECT * FROM Driver";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean update(Driver driver) throws SQLException {
        String sql = """
            UPDATE Driver
            SET Firstname = ?, Lastname = ?, Age = ?, Drive_license = ?
            WHERE DNI = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, driver.getFirstName());
            ps.setString(2, driver.getLastName());
            ps.setInt   (3, driver.getAge());
            ps.setString(4, driver.getTaxiLicense());
            ps.setString(5, driver.getNationalId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = """
                DELETE FROM Driver
                WHERE ID_Driver = ?        
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
        }
    }
}
