package logic_services;
import java.util.ArrayList;
import project_models.*;

/**
 * Core business logic layer.
 * Manages active service requests, the waiting list, and the taxi fleet.
 * Implements Sendable to support system-wide message notifications.
 */

public class ServiceManager implements Sendable {

    private ArrayList<ServiceRequest> activeServices; // currently running services
    private ArrayList<ServiceRequest> waitingList;    // requests pending a free taxi
    private int maxActive;                            // capacity limit of the services
    private int maxWaitingItems;                      // waiting list capacity limit
    private Sendable messenger;
    private ArrayList<Taxi> taxis;                    // registered taxis in the fleet

    public ServiceManager(Sendable messenger) {
        this.activeServices = new ArrayList<>();
        this.waitingList = new ArrayList<>();
        this.maxActive = 50;
        this.maxWaitingItems = 10;
        this.messenger = messenger;
        this.taxis = new ArrayList<>();
    }

    public ArrayList<ServiceRequest> getActiveServices() {
        return activeServices;
    }

    public ArrayList<ServiceRequest> getWaitingList() {
        return waitingList;
    }

    public int getMaxWaitingItems() {
        return maxWaitingItems;
    }

    public ArrayList<Taxi> getTaxis() {
        return taxis;
    }

    // this method should be called from the menu when a service is completed
    public void endService(ServiceRequest serviceToEnd, Position finalPosition){
        // updated the taxi's status and position
        Taxi taxi = serviceToEnd.getTaxi();
        taxi.setPosition(finalPosition);
        taxi.setStatus(TaxiStatus.AVAILABLE);

        // update service
        serviceToEnd.setServiceStatus(ServiceStatus.COMPLETED);

        // move service

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
        if (getActiveServices().size() > maxActive){
            Taxi newtaxi = findNearestTaxi(newService);

                // check if a taxi is available
            if (newtaxi != null){
                assignTaxiToService(newService, newtaxi);
                getActiveServices().add(newService);
                messenger.sendMessage("¡Assigned taxi! License Plate: " + newtaxi.getLicensePlate());
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
                // verify conditions
            if (taxi.getStatus() == TaxiStatus.AVAILABLE &&
                    taxi.getType() == serviceRequest.getTaxi().getType()){

                // apply distance manhattan
                int distance = calculateDistanceByManhattan(taxi.getPosition(), serviceRequest.getTaxi().getPosition());

                // updated the minimum distance and taxi rates
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
        // updated the service and taxi status
        service.setTaxi(taxi);
        service.setServiceStatus(ServiceStatus.IN_PROGRESS);

        // changed the taxi status
        taxi.setStatus(TaxiStatus.BUSY);
    }

    private void addServiceToWaitingList(ServiceRequest service) {
        if (this.getWaitingList().size() < maxWaitingItems) {
            service.setServiceStatus(ServiceStatus.PENDING);
            this.getWaitingList().add(service);
            sendMessage(service.getCustomer().getFirstName() +
                    "You have been moved to the waiting list, POSITION: " + getWaitingList().size());
        }else {
            sendMessage(service.getCustomer().getFirstName() +
                    "The service cannot be registered, try it later");
        }
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }
}