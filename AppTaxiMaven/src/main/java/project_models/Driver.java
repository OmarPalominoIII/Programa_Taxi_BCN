package project_models;

/**
 taxi driver. Extends Person.
 */
public class Driver extends Person {
    private int idDriver;
    private String taxiLicense;

    public int getIdDriver() {
        return idDriver;
    }

    public void setIdDriver(int idDriver) {
        this.idDriver = idDriver;
    }

    public Driver(String firstName, String lastName, int age, String nationalId, String taxiLicense) {
        super(firstName, lastName, age, nationalId);
        this.taxiLicense = taxiLicense;
        this.idDriver = 0;
    }

    public String getTaxiLicense()                { return taxiLicense; }
    public void setTaxiLicense(String taxiLicense){ this.taxiLicense = taxiLicense; }

    @Override
    public String toString() {
        return "Driver | ID Driver: " + idDriver + super.toString() + ", Driver license: " + taxiLicense;
    }
}
