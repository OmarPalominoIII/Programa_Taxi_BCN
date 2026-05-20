package manager;
import java.util.ArrayList;

import dao.impl.ServiceRequestImplDAO;
import dao.impl.TaxiImplDAO;
import models.*;
import services.Sendable;

/**
 * Core business logic layer.
 * Manages active service requests, the waiting list, and the taxi fleet.
 * Implements Sendable to support system-wide message notifications.
 */

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class ServiceManager {

    private ArrayList<ServiceRequest> activeServices; // currently running services
    private ArrayList<ServiceRequest> waitingList;    // requests pending a free taxi
    private int maxActive;                            // capacity limit of the services
    private int maxWaitingItems;                      // waiting list capacity limit
    private Sendable messenger;                       // interface
    private ArrayList<Taxi> taxis;                    // registered taxis in the fleet
    private ReportManager reportManager;              // import methods of this class


    private final ServiceRequestImplDAO serviceRequestsDAO;
    private final TaxiImplDAO taxiDAO;

    public ArrayList<ServiceRequest> getActiveServices() {
        return activeServices;
    }

    public ArrayList<ServiceRequest> getWaitingList() {
        return waitingList;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public int getMaxWaitingItems() {
        return maxWaitingItems;
    }

    public Sendable getMessenger() {
        return messenger;
    }

    public ArrayList<Taxi> getTaxis() {
        return taxis;
    }


    public ServiceManager(ReportManager reportManager, Sendable messenger,
                          ServiceRequestImplDAO serviceRequestsDAO, TaxiImplDAO taxiDAO) {
        this.activeServices = new ArrayList<>();
        this.waitingList = new ArrayList<>();
        this.maxActive = 50;
        this.maxWaitingItems = 10;
        this.messenger = messenger;
        this.reportManager = reportManager;
        this.serviceRequestsDAO = serviceRequestsDAO;
        this.taxiDAO = taxiDAO;


        try {
            this.taxis = taxiDAO.findAllTaxis();
        } catch (SQLException e) {
            messenger.sendMessage("Error crítico: No se pudo cargar la flota de taxis.");
            this.taxis = new ArrayList<>();
        }
    }


    public void endService(ServiceRequest serviceToEnd, Position finalPosition) {
        try {
            Taxi taxi = serviceToEnd.getTaxi();

            if (taxi != null && taxi.getIdTaxi() == 0) {
                Optional<Taxi> dbTaxi = taxiDAO.findByLicensePlate(taxi.getLicensePlate());
                if (dbTaxi.isPresent()) {
                    taxi.setIdTaxi(dbTaxi.get().getIdTaxi());
                }
            }
            serviceToEnd.setServiceStatus(ServiceStatus.COMPLETED);
            serviceRequestsDAO.updateStatus(serviceToEnd.getServiceCode(), ServiceStatus.COMPLETED);
            this.reportManager.addAttendedService(serviceToEnd);

            if (taxi != null) {
                taxi.setPosition(finalPosition);
                taxi.setStatus(TaxiStatus.AVAILABLE);
                taxiDAO.updatePosition(taxi.getIdTaxi(), finalPosition);
                taxiDAO.updateStatus(taxi.getIdTaxi(), TaxiStatus.AVAILABLE);
                messenger.sendMessage("¡Service completed! Taxi Available: " + taxi.getLicensePlate());

                // reassign taxi
                reassignTaxi(taxi);
            } else {
                messenger.sendMessage("¡Pending service cancelled successfully!");
            }

            this.getActiveServices().remove(serviceToEnd);
            this.getWaitingList().remove(serviceToEnd);

        } catch (SQLException e) {
            messenger.sendMessage("Error de base de datos al finalizar servicio: " + e.getMessage());
        }
    }

    private void reassignTaxi(Taxi taxiAvailable) {
        ServiceRequest pendingService = null;

        for (int i = 0; i < this.getWaitingList().size(); i++) {
            ServiceRequest s = getWaitingList().get(i);
            if (s.getTaxirequired() == taxiAvailable.getType()) {
                pendingService = s;
                getWaitingList().remove(s);
                break;
            }
        }

        if (pendingService != null) {
            assignTaxiToService(pendingService, taxiAvailable);
            this.getActiveServices().add(pendingService);
            messenger.sendMessage("The taxi has been reassigned to a waiting customer");
        } else {
            messenger.sendMessage("There are no customers waiting for this type of taxi");
        }
    }

    // registra una solicitud entrante
    public void registeredService(ServiceRequest newService) {
        if (getActiveServices().size() < maxActive) {


            if (newService.getRequestTime() == null) {
                newService.setRequestTime(LocalDateTime.now());
            }

            Taxi nearestTaxi = findNearestTaxi(newService);

            try {
                if (nearestTaxi != null) {

                    assignTaxiToService(newService, nearestTaxi);

                    int generatedId = serviceRequestsDAO.createServiceRequest(newService);
                    newService.setServiceCode(generatedId);

                    getActiveServices().add(newService);

                    double time = reportManager.calculateSingleServiceRouteTime(nearestTaxi, newService);
                    messenger.sendMessage("¡Assigned taxi! License Plate: " + nearestTaxi.getLicensePlate());
                    messenger.sendMessage("Estimated arrival time: " + time + " min");
                } else {
                    addServiceToWaitingList(newService);
                }
            } catch (SQLException e) {
                messenger.sendMessage("Error al registrar el servicio en la BD: " + e.getMessage());
            }
        }
    }

    private Taxi findNearestTaxi(ServiceRequest serviceRequest) {
        Taxi taxidesignated = null;
        double minimumDistance = Double.MAX_VALUE;

        try {
            this.taxis = taxiDAO.findAllTaxis();
        } catch (SQLException e) {
            System.err.println("Error refresh all taxis: " + e.getMessage());
        }

        for (Taxi taxi : this.getTaxis()) {
            if (taxi.getStatus() == TaxiStatus.AVAILABLE &&
                    taxi.getType() == serviceRequest.getTaxirequired()) {

                double distance = calculateDistanceByHaversine(taxi.getPosition(), serviceRequest.getCustomerPosition());

                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    taxidesignated = taxi;
                }
            }
        }
        return taxidesignated;
    }

    private double calculateDistanceByHaversine(Position taxi, Position service) {
        final int radioTierraKm = 6371;

        double latRad1 = Math.toRadians(taxi.getLatitude());
        double latRad2 = Math.toRadians(service.getLatitude());
        double deltaLat = Math.toRadians(taxi.getLatitude() - service.getLatitude());
        double deltaLon = Math.toRadians(taxi.getLongitude() - service.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(latRad1) * Math.cos(latRad2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radioTierraKm * c;
    }

    private void assignTaxiToService(ServiceRequest service, Taxi taxi) {

        if (taxi != null && taxi.getIdTaxi() <= 0) {
            try {
                Optional<Taxi> dbTaxi = taxiDAO.findByLicensePlate(taxi.getLicensePlate());
                if (dbTaxi.isPresent()) {
                    taxi.setIdTaxi(dbTaxi.get().getIdTaxi());
                }
            } catch (SQLException e) {
                System.err.println("Error al rescatar ID del taxi en la asignación: " + e.getMessage());
            }
        }
        service.setTaxi(taxi);
        service.setServiceStatus(ServiceStatus.IN_PROGRESS);

        if (taxi != null) {
            taxi.setStatus(TaxiStatus.BUSY);
            try {
                taxiDAO.updateStatus(taxi.getIdTaxi(), TaxiStatus.BUSY);

                if (service.getServiceCode() > 0) {
                    serviceRequestsDAO.assignTaxi(service.getServiceCode(), taxi.getIdTaxi());
                }
            } catch (SQLException e) {
                System.err.println("Error al actualizar estado ocupado del taxi: " + e.getMessage());
            }
        }
    }

    private void addServiceToWaitingList(ServiceRequest service) throws SQLException {
        if (this.getWaitingList().size() < maxWaitingItems) {
            service.setServiceStatus(ServiceStatus.PENDING);


            int generatedId = serviceRequestsDAO.createServiceRequest(service);
            service.setServiceCode(generatedId);

            this.getWaitingList().add(service);
            messenger.sendMessage(service.getCustomer().getFirstName() +
                    ", you have been moved to the waiting list, POSITION: " + getWaitingList().size());
        } else {
            messenger.sendMessage(service.getCustomer().getFirstName() +
                    "The service cannot be registered, try it later");
        }
    }

    public void markArrival(int serviceCode) {
        ServiceRequest service = null;

        for (ServiceRequest s : activeServices) {
            if (s.getServiceCode() == serviceCode) {
                service = s;
                break;
            }
        }

        if (service != null) {
            service.setServiceStatus(ServiceStatus.IN_PROGRESS);


            try {
                serviceRequestsDAO.updateStatus(serviceCode, ServiceStatus.IN_PROGRESS);
            } catch (SQLException e) {
                System.err.println("Error al actualizar llegada en BD: " + e.getMessage());
            }

            messenger.sendMessage("The taxi has arrived! The client has the number of the vehicle");
        } else {
            messenger.sendMessage("No active service found with the code:" + serviceCode);
        }
    }
}