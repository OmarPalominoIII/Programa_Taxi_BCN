package databaseDao;

import project_models.*;
import project_models.Driver;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceRequestDAOImpl implements ServiceRequestDAO {

    private final Connection connection;

    // Base SQL with JOINs to fetch fully populated Customer and Taxi data
    private static final String SELECT_BASE = """
        SELECT s.*,
               cl.first_name     AS cl_first_name,
               cl.last_name      AS cl_last_name,
               cl.age            AS cl_age,
               cl.national_id    AS cl_national_id,
               cl.phone_number   AS cl_phone_number,
               t.license_plate   AS t_license_plate,
               t.color           AS t_color,
               t.capacity        AS t_capacity,
               t.row_pos         AS t_row_pos,
               t.col_pos         AS t_col_pos,
               t.type            AS t_type,
               t.status          AS t_status,
               c.first_name      AS c_first_name,
               c.last_name       AS c_last_name,
               c.age             AS c_age,
               c.national_id     AS c_national_id,
               c.taxi_license    AS c_taxi_license
        FROM services s
        LEFT JOIN customers   cl ON s.customer_id = cl.id
        LEFT JOIN taxis        t ON s.taxi_id     = t.id
        LEFT JOIN drivers      c ON t.driver_id   = c.id
    """;

    public ServiceRequestDAOImpl(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────────────────────

    @Override
    public int insert(ServiceRequest service) throws SQLException {
        CustomerDAOImpl customerDAO = new CustomerDAOImpl(connection);
        int customerId = customerDAO.insert(service.getCustomer());

        int taxiId = -1;
        if (service.getTaxi() != null) {
            TaxiDAOImpl taxiDAO = new TaxiDAOImpl(connection);
            taxiId = taxiDAO.insert(service.getTaxi());
        }

        String sql = """
            INSERT OR IGNORE INTO services
                (service_code, customer_id, taxi_id, pickup_row, pickup_col,
                 taxi_type_required, status, request_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, service.getServiceCode());
            ps.setInt   (2, customerId);
            if (taxiId > 0) ps.setInt(3, taxiId);
            else            ps.setNull(3, Types.INTEGER);
            ps.setInt   (4, service.getCustomerPosition().getRow());
            ps.setInt   (5, service.getCustomerPosition().getColumn());
            ps.setString(6, service.getTaxirequired().name());
            ps.setString(7, service.getServiceStatus().name());
            ps.setString(8, service.getRequestTime().toString());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SELECT
    // ─────────────────────────────────────────────────────────────

    @Override
    public Optional<ServiceRequest> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE s.id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ServiceRequest> findByCode(int code) throws SQLException {
        String sql = SELECT_BASE + " WHERE s.service_code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<ServiceRequest> findAll() throws SQLException {
        List<ServiceRequest> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(SELECT_BASE)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<ServiceRequest> findByStatus(ServiceStatus status) throws SQLException {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<ServiceRequest> findByCustomerId(int customerId) throws SQLException {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.customer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean updateStatus(int id, ServiceStatus status) throws SQLException {
        String sql = "UPDATE services SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean assignTaxi(int serviceId, int taxiId) throws SQLException {
        String sql = "UPDATE services SET taxi_id = ?, status = 'IN_PROGRESS' WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taxiId);
            ps.setInt(2, serviceId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM services WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────────────────────────────

    private ServiceRequest mapRow(ResultSet rs) throws SQLException {
        // Customer
        Customer customer = new Customer(
            rs.getString("cl_first_name"),
            rs.getString("cl_last_name"),
            rs.getInt   ("cl_age"),
            rs.getString("cl_national_id"),
            rs.getString("cl_phone_number")
        );

        // Pickup Position
        Position pickupPosition = new Position(
            rs.getInt("pickup_row"),
            rs.getInt("pickup_col")
        );

        // Required Taxi Type
        TaxiType requiredType = TaxiType.valueOf(rs.getString("taxi_type_required"));

        // Build base ServiceRequest
        ServiceRequest service = new ServiceRequest(
            rs.getInt("service_code"),
            customer,
            pickupPosition,
            requiredType
        );

        // Status and request time
        service.setServiceStatus(ServiceStatus.valueOf(rs.getString("status")));
        service.setRequestTime(LocalTime.parse(rs.getString("request_time")));

        // Assigned Taxi (can be null)
        String licensePlate = rs.getString("t_license_plate");
        if (licensePlate != null) {
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

            Position taxiPosition = new Position(rs.getInt("t_row_pos"), rs.getInt("t_col_pos"));
            Taxi taxi = new Taxi(
                licensePlate,
                rs.getString("t_color"),
                rs.getInt   ("t_capacity"),
                driver,
                taxiPosition,
                TaxiType.valueOf(rs.getString("t_type"))
            );
            taxi.setStatus(TaxiStatus.valueOf(rs.getString("t_status")));
            service.setTaxi(taxi);
        }

        return service;
    }
}
