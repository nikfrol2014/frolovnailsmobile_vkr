package com.example.frolovnails.network.models.request;

public class UpdateClientRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String birthDate;
    private String notes;

    public UpdateClientRequest() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}