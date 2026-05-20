package com.example.frolovnails.network.models.request;

public class CreateMasterAppointmentRequest {
    private Long serviceId;
    private String startTime;
    private Long clientId;
    private String clientPhone;
    private String clientName;
    private String clientLastName;
    private String notes;

    public CreateMasterAppointmentRequest() {}

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientLastName() { return clientLastName; }
    public void setClientLastName(String clientLastName) { this.clientLastName = clientLastName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isValid() {
        if (clientId != null) return true;
        return clientPhone != null && !clientPhone.isEmpty() && clientName != null && !clientName.isEmpty();
    }
}