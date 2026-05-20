package dao;

import models.Position;
import models.Taxi;
import models.TaxiStatus;
import models.TaxiType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public interface TaxiDAO {
    int createTaxi(Taxi taxi)                                  throws SQLException;
    Optional<Taxi> findTaxiById(int id)                                throws SQLException;
    Optional<Taxi> findByLicensePlate(String licensePlate)   throws SQLException;
    ArrayList<Taxi> findAllTaxis()                           throws SQLException;
    ArrayList<Taxi> findByStatus(TaxiStatus status)          throws SQLException;
    ArrayList<Taxi> findByType(TaxiType type)                throws SQLException;
    ArrayList<Taxi> findAvailableByType(TaxiType type)       throws SQLException;
    boolean update(Taxi taxi)                                throws SQLException;
    boolean updatePosition(int id, Position position)        throws SQLException;
    boolean updateStatus(int id, TaxiStatus status)          throws SQLException;
}
