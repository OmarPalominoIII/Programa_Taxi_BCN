package dao.impl;

import dao.ServiceRequestsDAO;
import models.*;
import models.Driver;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class ServiceRequestImplDAO implements ServiceRequestsDAO {

    private final Connection conn;

    public ServiceRequestImplDAO(Connection conn) {
        this.conn = conn;
    }

    // SQL database perfectly aligned with the actual names of your tables and columns
    private static final String SELECT_BASE = """
        SELECT s.*,
               cl.Firstname      AS cl_firstname,
               cl.Lastname       AS cl_lastname,
               cl.Age            AS cl_age,
               cl.DNI            AS cl_dni,
               cl.Phone          AS cl_phone,
               t.Car_license     AS t_carlicense,
               t.Color           AS t_color,
               t.Capacity        AS t_capacity,
               t.Longitude       AS t_longitude,
               t.Latitude        AS t_latitude,
               t.Type            AS t_type,
               t.State           AS t_state,
               c.Firstname       AS c_firstname,
               c.Lastname       AS c_lastname,
               c.Age             AS c_age,
               c.DNI             AS c_dni,
               c.Driver_license  AS c_driverlicense
        FROM Services s
        LEFT JOIN Customer   cl ON s.ID_Customer = cl.ID_Customer
        LEFT JOIN Taxi        t ON s.ID_Taxi     = t.ID_Taxi
        LEFT JOIN Driver      c ON t.ID_Driver   = c.ID_Driver
    """;

    private ServiceRequest mapRow(ResultSet rs) throws SQLException {

        // Customer Reconstruction (Actual fields: Firstname, Lastname, Age, ID, Phone)
        Customer customer = new Customer();
        customer.setFirstName(rs.getString("cl_firstname"));
        customer.setLastName(rs.getString("cl_lastname"));
        customer.setAge(rs.getInt("cl_age"));
        customer.setNationalId(rs.getString("cl_dni"));
        customer.setPhoneNumber(rs.getString("cl_phone"));

        // Actual pickup position using real coordinates (Latitude, Longitude)
        Position pickupPosition = new Position();
        pickupPosition.setLatitude(rs.getDouble("Latitude"));
        pickupPosition.setLongitude(rs.getDouble("Longitude"));

        // Type of taxi requested (Maps with the Taxi_Required column)
        TaxiType requiredType = TaxiType.valueOf(rs.getString("Taxi_Required"));

        // Construction of the main business object
        ServiceRequest service = new ServiceRequest();
        service.setServiceCode(rs.getInt("ID_Service")); // Usamos la clave primaria real
        service.setCustomer(customer);
        service.setCustomerPosition(pickupPosition);
        service.setTaxirequired(requiredType);

        //Status and Date/Time (Date_Time is saved as TEXT and parsed)
        service.setServiceStatus(ServiceStatus.valueOf(rs.getString("Service_State")));
        service.setRequestTime(LocalDateTime.parse(rs.getString("Date_Time")));

        //Reconstruction of the assigned Taxi if it is not null (ID_Taxi in DB)
        int idTaxiCheck = rs.getInt("ID_Taxi");
        if (!rs.wasNull()) {
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

            Position taxiPosition = new Position();
            taxiPosition.setLongitude(rs.getDouble("t_longitude"));
            taxiPosition.setLatitude(rs.getDouble("t_latitude"));

            Taxi taxi = new Taxi();
            taxi.setIdTaxi(idTaxiCheck);
            taxi.setLicensePlate(rs.getString("t_carlicense"));
            taxi.setColor(rs.getString("t_color"));
            taxi.setCapacity(rs.getInt("t_capacity"));
            taxi.setDriver(driver);
            taxi.setPosition(taxiPosition);
            taxi.setType(TaxiType.valueOf(rs.getString("t_type")));
            taxi.setStatus(TaxiStatus.valueOf(rs.getString("t_state")));

            service.setTaxi(taxi);
        }

        return service;
    }

    @Override
    public int createServiceRequest(ServiceRequest service) throws SQLException {

        int customerId = 0;
        if (service.getCustomer() != null) {
            customerId = service.getCustomer().getIdCustomer();
        }

        if (customerId <= 0) {
            customerId = 1;
        }

        int taxiId = -1;
        if (service.getTaxi() != null) {
            taxiId = service.getTaxi().getIdTaxi();
        }


        String sql = """
        INSERT INTO Services
            (ID_Customer, ID_Taxi, Latitude, Longitude, Taxi_Required, Service_State, Date_Time)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);

            if (taxiId > 0) {
                ps.setInt(2, taxiId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDouble(3, service.getCustomerPosition().getLatitude());
            ps.setDouble(4, service.getCustomerPosition().getLongitude());
            ps.setString(5, service.getTaxirequired().name());
            ps.setString(6, service.getServiceStatus().name());
            ps.setString(7, service.getRequestTime().toString());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    @Override
    public Optional<ServiceRequest> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE s.ID_Service = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<ServiceRequest> findAllRequests() throws SQLException {
        ArrayList<ServiceRequest> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(SELECT_BASE)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public ArrayList<ServiceRequest> findByStatus(ServiceStatus status) throws SQLException {
        ArrayList<ServiceRequest> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.Service_State = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public ArrayList<ServiceRequest> findByCustomerId(int customerId) throws SQLException {
        ArrayList<ServiceRequest> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.ID_Customer = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean updateStatus(int id, ServiceStatus status) throws SQLException {
        String sql = "UPDATE Services SET Service_State = ? WHERE ID_Service = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePosition(int id, Position position){
        String sql = "UPDATE Services SET Longitude = ?, Latitude = ? WHERE ID_Service = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, position.getLongitude());
            ps.setDouble(2, position.getLatitude());
            ps.setInt   (3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean assignTaxi(int serviceId, int taxiId) throws SQLException {
        String sql = "UPDATE Services SET ID_Taxi = ?, Service_State = 'IN_PROGRESS' WHERE ID_Service = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taxiId);
            ps.setInt(2, serviceId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Services WHERE ID_Service = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
