package com.example.frolovnails.network.models.request;

public class CreateAvailableDayRequest {
    private String date;
    private String workStart;
    private String workEnd;
    private String notes;

    public CreateAvailableDayRequest() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getWorkStart() { return workStart; }
    public void setWorkStart(String workStart) { this.workStart = workStart; }

    public String getWorkEnd() { return workEnd; }
    public void setWorkEnd(String workEnd) { this.workEnd = workEnd; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}