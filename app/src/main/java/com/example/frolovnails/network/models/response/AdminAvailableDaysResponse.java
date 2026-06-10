package com.example.frolovnails.network.models.response;

import java.io.Serializable;
import java.util.List;

public class AdminAvailableDaysResponse implements Serializable {
    private int count;
    private List<AvailableDay> days;

    public AdminAvailableDaysResponse() {}

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<AvailableDay> getDays() { return days; }
    public void setDays(List<AvailableDay> days) { this.days = days; }
}