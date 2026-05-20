package com.example.frolovnails.network.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String birthDate;
    private String notes;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String firstName, String lastName, String birthDate, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.notes = notes;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}