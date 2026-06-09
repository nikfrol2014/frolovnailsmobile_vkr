package com.example.frolovnails.network.models.response;

import java.io.Serializable;
import java.util.List;

public class AvailableDaysResponse implements Serializable {
    private int daysCount;
    private int count;
    private List<AvailableDay> availableDays;

    public AvailableDaysResponse() {}

    public int getDaysCount() { return daysCount; }
    public void setDaysCount(int daysCount) { this.daysCount = daysCount; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<AvailableDay> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<AvailableDay> availableDays) { this.availableDays = availableDays; }

    public List<AvailableDay> getDays(){
        return availableDays;
    }
    public void setDays(List<AvailableDay> availableDays) { this.availableDays = availableDays; }
}