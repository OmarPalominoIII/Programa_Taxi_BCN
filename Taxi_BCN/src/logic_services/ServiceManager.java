package logic_services;
import java.util.ArrayList;
import project_models.*;
import report_view_logic.*;

/**
 * Core business logic layer.
 * Manages active service requests, the waiting list, and the taxi fleet.
 * Implements Sendable to support system-wide message notifications.
 */

public class ServiceManager {

    private ArrayList<ServiceRequest> activeServices; // currently running services
    private ArrayList<ServiceRequest> waitingList;    // requests pending a free taxi
    private int maxActive;                            // capacity limit of the services
    private int maxWaitingItems;                      // waiting list capacity limit
    private Sendable messenger;                       // interface
    private ArrayList<Taxi> taxis;                    // registered taxis in the fleet
    private ReportManager reportManager;              // import methods of this class

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

    public ServiceManager(ReportManager reportManager, Sendable messenger) {
        this.activeServices = new ArrayList<>();
        this.waitingList = new ArrayList<>();
        this.maxActive = 50;
        this.maxWaitingItems = 10;
        this.messenger = messenger;
        this.reportManager = reportManager;
        this.taxis = new ArrayList<>();
    }

    // this method should be called from the menu when a service is completed
    public void endService(ServiceRequest serviceToEnd, Position finalPosition){
        // move service
        this.reportManager.addAttendedService(serviceToEnd);

        // update service
        serviceToEnd.setServiceStatus(ServiceStatus.COMPLETED);

        // updated the taxi's status and position
        Taxi taxi = serviceToEnd.getTaxi();
        taxi.setPosition(finalPosition);
        taxi.setStatus(TaxiStatus.AVAILABLE);

        // clean servicio of the list of active services
        this.getActiveServices().remove(serviceToEnd);
        messenger.sendMessage("¡Service completed! Taxi Available: " + taxi.getLicensePlate());

        // reassign taxi
        reassignTaxi(taxi);
    }

    private void reassignTaxi(Taxi taxiAvailable){
        ServiceRequest pendingService = null;

        for (int i = 0; i < this.getWaitingList().size(); i++){
            ServiceRequest s = getWaitingList().get(i);
            if (s.getTaxirequired() == taxiAvailable.getType()){
                pendingService = s;
                getWaitingList().remove(s);
                break;
            }
        }

        if (pendingService != null){
            assignTaxiToService(pendingService, taxiAvailable);
            this.getActiveServices().add(pendingService);
            messenger.sendMessage("The taxi has been reassigned to a waiting customer");
        }else {
            messenger.sendMessage("There are no customers waiting for this type of taxi");
        }
    }

    // this method registers a taxi using the private methods below
    public void registeredService(ServiceRequest newService){
        if (getActiveServices().size() < maxActive){
            Taxi newtaxi = findNearestTaxi(newService);

                // check if a taxi is available
            if (newtaxi != null){
                assignTaxiToService(newService, newtaxi);
                getActiveServices().add(newService);
                double time = reportManager.calculateSingleServiceRouteTime(newtaxi, newService);
                messenger.sendMessage("¡Assigned taxi! License Plate: " + newtaxi.getLicensePlate());
                messenger.sendMessage("Estimated arrival time: " + time + " min");
            }else {
                // add service to waiting list
                addServiceToWaitingList(newService);
            }
        }
    }

    private Taxi findNearestTaxi(ServiceRequest serviceRequest){
        Taxi taxidesignated = null;
        int minimumDistance = Integer.MAX_VALUE;

        for (Taxi taxi : this.getTaxis()){
            if (taxi.getStatus() == TaxiStatus.AVAILABLE &&
                    taxi.getType() == serviceRequest.getTaxirequired()){

                int distance = calculateDistanceByManhattan(taxi.getPosition(), serviceRequest.getCustomerPosition());

                if (distance < minimumDistance){
                    minimumDistance = distance;
                    taxidesignated = taxi;
                }
            }
        }
        return taxidesignated;
    }

    private int calculateDistanceByManhattan(Position taxi, Position service){
        return Math.abs(taxi.getRow() - service.getRow()) + Math.abs(taxi.getColumn() - service.getColumn());
    }

    private void assignTaxiToService(ServiceRequest service, Taxi taxi){
        service.setTaxi(taxi);
        service.setServiceStatus(ServiceStatus.IN_PROGRESS);

        taxi.setStatus(TaxiStatus.BUSY);
    }

    private void addServiceToWaitingList(ServiceRequest service) {
        if (this.getWaitingList().size() < maxWaitingItems) {
            service.setServiceStatus(ServiceStatus.PENDING);
            this.getWaitingList().add(service);
            messenger.sendMessage(service.getCustomer().getFirstName() +
                    "You have been moved to the waiting list, POSITION: " + getWaitingList().size());
        }else {
            messenger.sendMessage(service.getCustomer().getFirstName() +
                    "The service cannot be registered, try it later");
        }
    }

    public void marcarArribada(int serviceCode) {
        ServiceRequest servei = null;

        for (ServiceRequest s : activeServices) {
            if (s.getServiceCode() == serviceCode) {
                servei = s;
                break;
            }
        }

        if (servei != null) {
            servei.setServiceStatus(ServiceStatus.IN_PROGRESS);
            messenger.sendMessage("¡El taxi ha arribat! El client ja és dins del vehicle.");
        } else {
            messenger.sendMessage("No s'ha trobat cap servei actiu amb el codi: " + serviceCode);
        }
    }

}