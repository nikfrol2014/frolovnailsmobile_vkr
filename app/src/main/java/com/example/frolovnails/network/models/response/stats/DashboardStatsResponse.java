package com.example.frolovnails.network.models.response.stats;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStatsResponse {
    private PeriodStats currentPeriod;
    private PeriodStats previousPeriod;
    private ComparisonStats comparison;
    private List<DailyStats> dailyStats;
    private List<TopServiceStats> topServices;
    private List<TopClientStats> topClients;
    private List<HourlyStats> peakHours;

    public DashboardStatsResponse() {}

    // Getters and Setters
    public PeriodStats getCurrentPeriod() { return currentPeriod; }
    public void setCurrentPeriod(PeriodStats currentPeriod) { this.currentPeriod = currentPeriod; }

    public PeriodStats getPreviousPeriod() { return previousPeriod; }
    public void setPreviousPeriod(PeriodStats previousPeriod) { this.previousPeriod = previousPeriod; }

    public ComparisonStats getComparison() { return comparison; }
    public void setComparison(ComparisonStats comparison) { this.comparison = comparison; }

    public List<DailyStats> getDailyStats() { return dailyStats; }
    public void setDailyStats(List<DailyStats> dailyStats) { this.dailyStats = dailyStats; }

    public List<TopServiceStats> getTopServices() { return topServices; }
    public void setTopServices(List<TopServiceStats> topServices) { this.topServices = topServices; }

    public List<TopClientStats> getTopClients() { return topClients; }
    public void setTopClients(List<TopClientStats> topClients) { this.topClients = topClients; }

    public List<HourlyStats> getPeakHours() { return peakHours; }
    public void setPeakHours(List<HourlyStats> peakHours) { this.peakHours = peakHours; }

    // Вложенные классы
    public static class PeriodStats {
        private int totalAppointments;
        private int completedAppointments;
        private int cancelledAppointments;
        private int createdAppointments;
        private int confirmedAppointments;
        private int noShowAppointments;
        private BigDecimal totalRevenue;
        private BigDecimal averageCheck;
        private int newClientsCount;
        private double occupancyRate;
        private int workingDaysCount;

        public PeriodStats() {}

        // Getters and Setters
        public int getTotalAppointments() { return totalAppointments; }
        public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }

        public int getCompletedAppointments() { return completedAppointments; }
        public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }

        public int getCancelledAppointments() { return cancelledAppointments; }
        public void setCancelledAppointments(int cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }

        public int getCreatedAppointments() { return createdAppointments; }
        public void setCreatedAppointments(int createdAppointments) { this.createdAppointments = createdAppointments; }

        public int getConfirmedAppointments() { return confirmedAppointments; }
        public void setConfirmedAppointments(int confirmedAppointments) { this.confirmedAppointments = confirmedAppointments; }

        public int getNoShowAppointments() { return noShowAppointments; }
        public void setNoShowAppointments(int noShowAppointments) { this.noShowAppointments = noShowAppointments; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public BigDecimal getAverageCheck() { return averageCheck; }
        public void setAverageCheck(BigDecimal averageCheck) { this.averageCheck = averageCheck; }

        public int getNewClientsCount() { return newClientsCount; }
        public void setNewClientsCount(int newClientsCount) { this.newClientsCount = newClientsCount; }

        public double getOccupancyRate() { return occupancyRate; }
        public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }

        public int getWorkingDaysCount() { return workingDaysCount; }
        public void setWorkingDaysCount(int workingDaysCount) { this.workingDaysCount = workingDaysCount; }
    }

    public static class ComparisonStats {
        private double appointmentsChange;
        private double revenueChange;
        private double averageCheckChange;
        private double occupancyChange;

        public ComparisonStats() {}

        public double getAppointmentsChange() { return appointmentsChange; }
        public void setAppointmentsChange(double appointmentsChange) { this.appointmentsChange = appointmentsChange; }

        public double getRevenueChange() { return revenueChange; }
        public void setRevenueChange(double revenueChange) { this.revenueChange = revenueChange; }

        public double getAverageCheckChange() { return averageCheckChange; }
        public void setAverageCheckChange(double averageCheckChange) { this.averageCheckChange = averageCheckChange; }

        public double getOccupancyChange() { return occupancyChange; }
        public void setOccupancyChange(double occupancyChange) { this.occupancyChange = occupancyChange; }
    }

    public static class DailyStats {
        private String date;
        private int appointmentsCount;
        private BigDecimal revenue;
        private int completedCount;
        private int cancelledCount;
        private String dayOfWeek;

        public DailyStats() {}

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public int getAppointmentsCount() { return appointmentsCount; }
        public void setAppointmentsCount(int appointmentsCount) { this.appointmentsCount = appointmentsCount; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public int getCompletedCount() { return completedCount; }
        public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

        public int getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }

        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    }

    public static class TopServiceStats {
        private Long serviceId;
        private String serviceName;
        private String category;
        private int count;
        private BigDecimal totalRevenue;
        private BigDecimal averagePrice;
        private int durationMinutes;

        public TopServiceStats() {}

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    }

    public static class TopClientStats {
        private Long clientId;
        private String firstName;
        private String lastName;
        private String phone;
        private int totalVisits;
        private BigDecimal totalSpent;
        private BigDecimal averageCheck;
        private String lastVisitDate;
        private String favoriteService;

        public TopClientStats() {}

        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public int getTotalVisits() { return totalVisits; }
        public void setTotalVisits(int totalVisits) { this.totalVisits = totalVisits; }

        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

        public BigDecimal getAverageCheck() { return averageCheck; }
        public void setAverageCheck(BigDecimal averageCheck) { this.averageCheck = averageCheck; }

        public String getLastVisitDate() { return lastVisitDate; }
        public void setLastVisitDate(String lastVisitDate) { this.lastVisitDate = lastVisitDate; }

        public String getFavoriteService() { return favoriteService; }
        public void setFavoriteService(String favoriteService) { this.favoriteService = favoriteService; }
    }

    public static class HourlyStats {
        private int hour;
        private int appointmentsCount;
        private BigDecimal revenue;
        private double occupancyRate;

        public HourlyStats() {}

        public int getHour() { return hour; }
        public void setHour(int hour) { this.hour = hour; }

        public int getAppointmentsCount() { return appointmentsCount; }
        public void setAppointmentsCount(int appointmentsCount) { this.appointmentsCount = appointmentsCount; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public double getOccupancyRate() { return occupancyRate; }
        public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }
    }
}