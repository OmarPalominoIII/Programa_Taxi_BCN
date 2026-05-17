package project_models;
import java.time.LocalTime;

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
    private LocalTime requestTime;
    private TaxiType taxirequired;

    public ServiceRequest(int serviceCode, Customer customer, Position customerPosition, TaxiType taxirequired) {
        this.serviceCode = serviceCode;
        this.customer = customer;
        this.customerPosition = customerPosition;
        this.serviceStatus = ServiceStatus.PENDING; // always starts as pending
        this.requestTime = LocalTime.now();
        this.taxi = null;
        this.taxirequired = taxirequired;
    }

    // Getters
    public int getServiceCode()              { return serviceCode; }
    public Customer getCustomer()            { return customer; }
    public Position getCustomerPosition()    { return customerPosition; }
    public ServiceStatus getServiceStatus()  { return serviceStatus; }
    public Taxi getTaxi()                    { return taxi; }
    public LocalTime getRequestTime()        { return requestTime; }
    public TaxiType getTaxirequired()        {return taxirequired; }

    // Setters
    public void setServiceCode(int serviceCode)              { this.serviceCode = serviceCode; }
    public void setCustomer(Customer customer)               { this.customer = customer; }
    public void setCustomerPosition(Position customerPosition){ this.customerPosition = customerPosition; }
    public void setServiceStatus(ServiceStatus serviceStatus){ this.serviceStatus = serviceStatus; }
    public void setTaxi(Taxi taxi)                           { this.taxi = taxi; }
    public void setRequestTime(LocalTime requestTime)        { this.requestTime = requestTime; }
    public void setTaxirequired(TaxiType taxirequired)       { this.taxirequired = taxirequired; }

    @Override
    public String toString() {
        return "ID: " + serviceCode +
               ", Customer: " + customer.getFirstName() + " " + customer.getLastName() +
               ", Status: " + serviceStatus;
    }
}
