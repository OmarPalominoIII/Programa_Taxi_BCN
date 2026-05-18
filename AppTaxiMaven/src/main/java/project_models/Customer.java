package project_models;

public class Customer extends Person {
    private int idCustomer;
    private String phoneNumber;

    public int getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(int idCustomer) {
        this.idCustomer = idCustomer;
    }

    public Customer(String firstName, String lastName, int age, String nationalId, String phoneNumber) {
        super(firstName, lastName, age, nationalId);
        this.idCustomer = 0;
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()                 { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return "Customer | Id customer:" + idCustomer + super.toString() + ", phoneNumber='" + phoneNumber + "'}";
    }
}
