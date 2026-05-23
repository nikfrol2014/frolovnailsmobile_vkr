package com.example.frolovnails.network.models.response;

import java.io.Serializable;

public class Appointment implements Serializable {
    private Long id;
    private ClientInfo client;
    private ServiceInfo service;
    private String startTime;
    private String endTime;
    private AppointmentStatus status;
    private String clientNotes;
    private String masterNotes;
    private String createdAt;
    private String updatedAt;

    public Appointment() {}

    // Геттеры
    public Long getId() { return id; }
    public ClientInfo getClient() { return client; }
    public ServiceInfo getService() { return service; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public AppointmentStatus getStatus() { return status; }
    public String getClientNotes() { return clientNotes; }
    public String getMasterNotes() { return masterNotes; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Сеттеры
    public void setId(Long id) { this.id = id; }
    public void setClient(ClientInfo client) { this.client = client; }
    public void setService(ServiceInfo service) { this.service = service; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public void setClientNotes(String clientNotes) { this.clientNotes = clientNotes; }
    public void setMasterNotes(String masterNotes) { this.masterNotes = masterNotes; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public enum AppointmentStatus {
        CREATED, PENDING, CONFIRMED, CANCELLED, COMPLETED
    }

    public static class ClientInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;

        public ClientInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ServiceInfo {
        private Long id;
        private String name;
        private Integer durationMinutes;
        private java.math.BigDecimal price;
        private String category;

        public ServiceInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}