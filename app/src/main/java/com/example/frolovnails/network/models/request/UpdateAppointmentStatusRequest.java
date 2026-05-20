package com.example.frolovnails.network.models.request;

public class UpdateAppointmentStatusRequest {
    private String status;
    private String masterNotes;

    public UpdateAppointmentStatusRequest() {}

    public UpdateAppointmentStatusRequest(String status, String masterNotes) {
        this.status = status;
        this.masterNotes = masterNotes;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMasterNotes() { return masterNotes; }
    public void setMasterNotes(String masterNotes) { this.masterNotes = masterNotes; }
}