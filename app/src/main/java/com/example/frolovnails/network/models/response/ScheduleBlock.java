package com.example.frolovnails.network.models.response;

import java.io.Serializable;

public class ScheduleBlock implements Serializable {
    private Long id;
    private String startTime;   // dd.MM.yyyy HH:mm
    private String endTime;     // dd.MM.yyyy HH:mm
    private String reason;
    private String notes;
    private Boolean isBlocked;

    public ScheduleBlock() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }
}