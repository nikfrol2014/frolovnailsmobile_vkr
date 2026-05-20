package com.example.frolovnails.network.models.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelineResponse {
    private String startDate;
    private String endDate;
    private Integer totalDays;
    private Integer totalAppointments;
    private Map<String, List<Appointment>> appointmentsByDay;
    private TimelineStats stats;

    public TimelineResponse() {
        this.appointmentsByDay = new HashMap<>();
    }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }

    public Integer getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(Integer totalAppointments) { this.totalAppointments = totalAppointments; }

    public Map<String, List<Appointment>> getAppointmentsByDay() {
        return appointmentsByDay != null ? appointmentsByDay : new HashMap<>();
    }
    public void setAppointmentsByDay(Map<String, List<Appointment>> appointmentsByDay) {
        this.appointmentsByDay = appointmentsByDay;
    }

    public TimelineStats getStats() { return stats; }
    public void setStats(TimelineStats stats) { this.stats = stats; }

    public static class TimelineStats {
        private Long confirmedCount;
        private Long pendingCount;
        private Long cancelledCount;
        private Long completedCount;
        private Long createdCount;
        private Integer workingDaysCount;
        private Integer daysWithAppointments;

        public TimelineStats() {}

        public Long getConfirmedCount() { return confirmedCount; }
        public void setConfirmedCount(Long confirmedCount) { this.confirmedCount = confirmedCount; }

        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }

        public Long getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(Long cancelledCount) { this.cancelledCount = cancelledCount; }

        public Long getCompletedCount() { return completedCount; }
        public void setCompletedCount(Long completedCount) { this.completedCount = completedCount; }

        public Long getCreatedCount() { return createdCount; }
        public void setCreatedCount(Long createdCount) { this.createdCount = createdCount; }

        public Integer getWorkingDaysCount() { return workingDaysCount; }
        public void setWorkingDaysCount(Integer workingDaysCount) { this.workingDaysCount = workingDaysCount; }

        public Integer getDaysWithAppointments() { return daysWithAppointments; }
        public void setDaysWithAppointments(Integer daysWithAppointments) { this.daysWithAppointments = daysWithAppointments; }
    }
}