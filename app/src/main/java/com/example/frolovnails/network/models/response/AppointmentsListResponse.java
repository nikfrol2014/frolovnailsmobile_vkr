package com.example.frolovnails.network.models.response;

import java.util.List;

public class AppointmentsListResponse {
    private List<Appointment> appointments;
    private int count;

    public AppointmentsListResponse() {}

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}