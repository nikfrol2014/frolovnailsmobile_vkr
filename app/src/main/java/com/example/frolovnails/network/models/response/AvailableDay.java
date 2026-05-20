package com.example.frolovnails.network.models.response;

public class AvailableDay {
    private Long id;
    private String availableDate;
    private String workStart;
    private String workEnd;
    private Boolean isAvailable;
    private String notes;

    public AvailableDay() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAvailableDate() { return availableDate; }
    public void setAvailableDate(String availableDate) { this.availableDate = availableDate; }

    public String getWorkStart() { return workStart; }
    public void setWorkStart(String workStart) { this.workStart = workStart; }

    public String getWorkEnd() { return workEnd; }
    public void setWorkEnd(String workEnd) { this.workEnd = workEnd; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}