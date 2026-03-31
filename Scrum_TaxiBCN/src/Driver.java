/**
 taxi driver. Extends Person.
 */
public class Driver extends Person {

    private String taxiLicense;

    public Driver(String firstName, String lastName, int age, String nationalId, String taxiLicense) {
        super(firstName, lastName, age, nationalId);
        this.taxiLicense = taxiLicense;
    }

    public String getTaxiLicense()                { return taxiLicense; }
    public void setTaxiLicense(String taxiLicense){ this.taxiLicense = taxiLicense; }

    @Override
    public String toString() {
        return "Driver{" + super.toString() + ", taxiLicense='" + taxiLicense + "'}";
    }
}
