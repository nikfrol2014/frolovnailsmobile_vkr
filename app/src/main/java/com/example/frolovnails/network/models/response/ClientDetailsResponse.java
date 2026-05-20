package com.example.frolovnails.network.models.response;

import java.util.List;

public class ClientDetailsResponse {
    private ClientInfo client;
    private ClientStats stats;
    private List<Appointment> recentAppointments;
    private List<Appointment> upcomingAppointments;

    public ClientDetailsResponse() {}

    public ClientInfo getClient() { return client; }
    public void setClient(ClientInfo client) { this.client = client; }

    public ClientStats getStats() { return stats; }
    public void setStats(ClientStats stats) { this.stats = stats; }

    public List<Appointment> getRecentAppointments() { return recentAppointments; }
    public void setRecentAppointments(List<Appointment> recentAppointments) { this.recentAppointments = recentAppointments; }

    public List<Appointment> getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(List<Appointment> upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }

    public static class ClientInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
        private String birthDate;
        private String notes;
        private String registeredAt;
        private Boolean isVip;

        public ClientInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

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

        public String getRegisteredAt() { return registeredAt; }
        public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }

        public Boolean getIsVip() { return isVip; }
        public void setIsVip(Boolean isVip) { this.isVip = isVip; }
    }

    public static class ClientStats {
        private Integer totalVisits;
        private Integer cancelledVisits;
        private Integer noShowVisits;
        private java.math.BigDecimal totalSpent;
        private java.math.BigDecimal averageBill;
        private String firstVisitDate;
        private String lastVisitDate;
        private String favoriteService;
        private Integer favoriteServiceCount;
        private String favoriteMaster;
        private Double attendanceRate;

        public ClientStats() {}

        public Integer getTotalVisits() { return totalVisits; }
        public void setTotalVisits(Integer totalVisits) { this.totalVisits = totalVisits; }

        public Integer getCancelledVisits() { return cancelledVisits; }
        public void setCancelledVisits(Integer cancelledVisits) { this.cancelledVisits = cancelledVisits; }

        public Integer getNoShowVisits() { return noShowVisits; }
        public void setNoShowVisits(Integer noShowVisits) { this.noShowVisits = noShowVisits; }

        public java.math.BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(java.math.BigDecimal totalSpent) { this.totalSpent = totalSpent; }

        public java.math.BigDecimal getAverageBill() { return averageBill; }
        public void setAverageBill(java.math.BigDecimal averageBill) { this.averageBill = averageBill; }

        public String getFirstVisitDate() { return firstVisitDate; }
        public void setFirstVisitDate(String firstVisitDate) { this.firstVisitDate = firstVisitDate; }

        public String getLastVisitDate() { return lastVisitDate; }
        public void setLastVisitDate(String lastVisitDate) { this.lastVisitDate = lastVisitDate; }

        public String getFavoriteService() { return favoriteService; }
        public void setFavoriteService(String favoriteService) { this.favoriteService = favoriteService; }

        public Integer getFavoriteServiceCount() { return favoriteServiceCount; }
        public void setFavoriteServiceCount(Integer favoriteServiceCount) { this.favoriteServiceCount = favoriteServiceCount; }

        public String getFavoriteMaster() { return favoriteMaster; }
        public void setFavoriteMaster(String favoriteMaster) { this.favoriteMaster = favoriteMaster; }

        public Double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }
    }
}