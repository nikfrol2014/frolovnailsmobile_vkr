package com.example.frolovnails.network.models.request;

public class CreateScheduleBlockRequest {
    private String startTime;
    private String endTime;
    private String reason;
    private String notes;

    public CreateScheduleBlockRequest() {}

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}