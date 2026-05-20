package com.example.frolovnails.network.models.response;

import java.util.List;

public class AvailableSlotsResponse {
    private String date;
    private List<String> availableSlots;
    private Integer count;
    private String slotDuration;
    private String note;

    public AvailableSlotsResponse() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<String> availableSlots) { this.availableSlots = availableSlots; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public String getSlotDuration() { return slotDuration; }
    public void setSlotDuration(String slotDuration) { this.slotDuration = slotDuration; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}