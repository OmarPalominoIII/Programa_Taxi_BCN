package databaseDao;

import project_models.ServiceRequest;
import project_models.ServiceStatus;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestDAO {
    int insert(ServiceRequest service)                           throws SQLException;
    Optional<ServiceRequest> findById(int id)                     throws SQLException;
    Optional<ServiceRequest> findByCode(int code)                 throws SQLException;
    List<ServiceRequest> findAll()                               throws SQLException;
    List<ServiceRequest> findByStatus(ServiceStatus status)       throws SQLException;
    List<ServiceRequest> findByCustomerId(int customerId)         throws SQLException;
    boolean updateStatus(int id, ServiceStatus status)           throws SQLException;
    boolean assignTaxi(int serviceId, int taxiId)                 throws SQLException;
    boolean delete(int id)                                        throws SQLException;
}
