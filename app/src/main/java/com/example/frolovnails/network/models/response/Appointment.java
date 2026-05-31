package com.example.frolovnails.network.models.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Object> metadata = new HashMap<>();

    // НОВЫЕ ПОЛЯ ДЛЯ МЕТАДАННЫХ (приходят отдельно от сервера)
    private BigDecimal actualPrice;
    private String actualServices;
    private String masterCompletionComment;

    public Appointment() {}

    // Геттеры и сеттеры для существующих полей
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ClientInfo getClient() { return client; }
    public void setClient(ClientInfo client) { this.client = client; }

    public ServiceInfo getService() { return service; }
    public void setService(ServiceInfo service) { this.service = service; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getClientNotes() { return clientNotes; }
    public void setClientNotes(String clientNotes) { this.clientNotes = clientNotes; }

    public String getMasterNotes() { return masterNotes; }
    public void setMasterNotes(String masterNotes) { this.masterNotes = masterNotes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // НОВЫЕ ГЕТТЕРЫ И СЕТТЕРЫ
    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }

    public String getActualServices() { return actualServices; }
    public void setActualServices(String actualServices) { this.actualServices = actualServices; }

    public String getMasterCompletionComment() { return masterCompletionComment; }
    public void setMasterCompletionComment(String masterCompletionComment) { this.masterCompletionComment = masterCompletionComment; }

    public enum AppointmentStatus {
        CREATED, PENDING, CONFIRMED, CANCELLED, COMPLETED
    }

    public static class ClientInfo implements Serializable {
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

    public static class ServiceInfo implements Serializable {
        private Long id;
        private String name;
        private Integer durationMinutes;
        private BigDecimal price;
        private String category;

        public ServiceInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}