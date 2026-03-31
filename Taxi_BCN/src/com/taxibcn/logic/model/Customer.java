package com.taxibcn.logic.model;

public class Customer extends Person {

    private String phoneNumber;

    public Customer(String firstName, String lastName, int age, String nationalId, String phoneNumber) {
        super(firstName, lastName, age, nationalId);
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()                 { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return "Customer{" + super.toString() + ", phoneNumber='" + phoneNumber + "'}";
    }
}
