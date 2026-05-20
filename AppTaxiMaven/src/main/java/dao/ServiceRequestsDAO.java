package dao;

import models.Position;
import models.ServiceRequest;
import models.ServiceStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public interface ServiceRequestsDAO {
        int createServiceRequest(ServiceRequest service)                           throws SQLException;
        Optional<ServiceRequest> findById(int id)                                throws SQLException;
        ArrayList<ServiceRequest> findAllRequests()                               throws SQLException;
        ArrayList<ServiceRequest> findByStatus(ServiceStatus status)       throws SQLException;
        ArrayList<ServiceRequest> findByCustomerId(int customerId)         throws SQLException;
        boolean updateStatus(int id, ServiceStatus status)           throws SQLException;
        boolean updatePosition(int id, Position position);
        boolean assignTaxi(int serviceId, int taxiId)                 throws SQLException;
        boolean delete(int id)                                        throws SQLException;

}
