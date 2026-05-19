package databaseDao;

import project_models.Taxi;
import project_models.TaxiStatus;
import project_models.TaxiType;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TaxiDAO {
    int insert(Taxi taxi)                                    throws SQLException;
    Optional<Taxi> findById(int id)                          throws SQLException;
    Optional<Taxi> findByLicensePlate(String licensePlate)   throws SQLException;
    List<Taxi> findAll()                                     throws SQLException;
    List<Taxi> findByStatus(TaxiStatus status)               throws SQLException;
    List<Taxi> findByType(TaxiType type)                     throws SQLException;
    List<Taxi> findAvailableByType(TaxiType type)            throws SQLException;
    boolean update(Taxi taxi)                                throws SQLException;
    boolean updateStatus(int id, TaxiStatus status)          throws SQLException;
    boolean delete(int id)                                   throws SQLException;
}
