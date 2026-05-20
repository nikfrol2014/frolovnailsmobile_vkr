package com.example.frolovnails.network.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class CreateAppointmentRequest {
    private Long serviceId;
    private String startTime;  // формат "dd.MM.yyyy HH:mm"
    private String clientNotes;

    public CreateAppointmentRequest() {
    }

    public CreateAppointmentRequest(Long serviceId, String startTime, String clientNotes) {
        this.serviceId = serviceId;
        this.startTime = startTime;
        this.clientNotes = clientNotes;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getClientNotes() {
        return clientNotes;
    }

    public void setClientNotes(String clientNotes) {
        this.clientNotes = clientNotes;
    }
}