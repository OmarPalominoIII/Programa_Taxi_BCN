package logic_services;
import java.util.ArrayList;

/**
 * Core business logic layer.
 * Manages active service requests, the waiting list, and the taxi fleet.
 * Implements Sendable to support system-wide message notifications.
 */
public class ServiceManager implements Sendable {

    private ArrayList<ServiceRequest> activeServices; // currently running services
    private ArrayList<ServiceRequest> waitingList;    // requests pending a free taxi
    private int maxWaitingItems;                      // waiting list capacity limit
    private ArrayList<Taxi> taxis;                    // registered taxis in the fleet

    public ServiceManager(int maxWaitingItems) {
        this.activeServices = new ArrayList<>();
        this.waitingList = new ArrayList<>();
        this.maxWaitingItems = maxWaitingItems;
        this.taxis = new ArrayList<>();
    }

    @Override
    public void sendMessage(String message) {

    }
}