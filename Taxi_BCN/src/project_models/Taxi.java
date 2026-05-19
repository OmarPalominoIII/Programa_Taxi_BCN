package project_models;


/**
 * Represents a taxi vehicle with its driver, position, type, and current status.
 */
public class Taxi {

    private String licensePlate;
    private String color;
    private int capacity;
    private Driver driver;
    private Position position;
    private TaxiType type;
    private TaxiStatus status;

    public Taxi(String licensePlate, String color, int capacity,
                Driver driver, Position position, TaxiType type) {
        this.licensePlate = licensePlate;
        this.color = color;
        this.capacity = capacity;
        this.driver = driver;
        this.position = position;
        this.type = type;
        this.status = TaxiStatus.AVAILABLE; // default state on creation
    }

    // Getters
    public String getLicensePlate() { return licensePlate; }
    public String getColor()        { return color; }
    public int getCapacity()        { return capacity; }
    public Driver getDriver()       { return driver; }
    public Position getPosition()   { return position; }
    public TaxiType getType()       { return type; }
    public TaxiStatus getStatus()   { return status; }

    // Setters
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public void setColor(String color)               { this.color = color; }
    public void setCapacity(int capacity)            { this.capacity = capacity; }
    public void setDriver(Driver driver)             { this.driver = driver; }
    public void setPosition(Position position)       { this.position = position; }
    public void setType(TaxiType type)               { this.type = type; }
    public void setStatus(TaxiStatus status)         { this.status = status; }

    @Override
    public String toString() {
        return "Taxi{licensePlate='" + licensePlate + "', color='" + color +
               "', capacity=" + capacity + ", type=" + type +
               ", status=" + status + ", position=" + position + "}";
    }
}
