package models;
import java.time.LocalDateTime;

/**
 * Represents a taxi service request made by a customer.
 * Links a customer, their pickup position, and the assigned taxi.
 */
public class ServiceRequest {

    private int serviceCode;
    private Customer customer;
    private Position customerPosition; // pickup location
    private ServiceStatus serviceStatus;
    private Taxi taxi;
    private LocalDateTime requestTime;
    private TaxiType taxirequired;

    @Override
    public String toString() {
        return "Service ID: " + serviceCode +
                ", Customer: " + (customer != null ? (customer.getFirstName() + " " + customer.getLastName()) : "NO CUSTOMER") +
                ", Taxi ID: " + (taxi != null ? taxi.getIdTaxi() : "NONE (Waiting for assignment)") +
                ", Datetime: " + requestTime +
                ", Coordinates: " + (customerPosition != null ? (customerPosition.getLatitude() + " " + customerPosition.getLongitude()) : "0.0 0.0") +
                ", Service status: " + serviceStatus +
                ", Taxi Required: " + taxirequired;
    }

    public ServiceRequest(Customer customer, Position customerPosition, TaxiType taxirequired) {
        this.serviceCode = 0;
        this.customer = customer;
        this.customerPosition = customerPosition;
        this.serviceStatus = ServiceStatus.PENDING; // always starts as pending
        this.requestTime = LocalDateTime.now();
        this.taxi = null;
        this.taxirequired = taxirequired;
    }

    public ServiceRequest(){}

    // Getters
    public int getServiceCode()              { return serviceCode; }
    public Customer getCustomer()            { return customer; }
    public Position getCustomerPosition()    { return customerPosition; }
    public ServiceStatus getServiceStatus()  { return serviceStatus; }
    public Taxi getTaxi()                    { return taxi; }
    public LocalDateTime getRequestTime()        { return requestTime; }
    public TaxiType getTaxirequired()        {return taxirequired; }

    // Setters
    public void setServiceCode(int serviceCode)              { this.serviceCode = serviceCode; }
    public void setCustomer(Customer customer)               { this.customer = customer; }
    public void setCustomerPosition(Position customerPosition){ this.customerPosition = customerPosition; }
    public void setServiceStatus(ServiceStatus serviceStatus){ this.serviceStatus = serviceStatus; }
    public void setTaxi(Taxi taxi)                           { this.taxi = taxi; }
    public void setRequestTime(LocalDateTime requestTime)        { this.requestTime = requestTime; }
    public void setTaxirequired(TaxiType taxirequired)       { this.taxirequired = taxirequired; }

}
