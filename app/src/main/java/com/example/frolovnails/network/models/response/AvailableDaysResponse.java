package com.example.frolovnails.network.models.response;

import java.util.List;

public class AvailableDaysResponse {
    private List<AvailableDay> days;
    private Integer count;

    public AvailableDaysResponse() {}

    public List<AvailableDay> getDays() { return days; }
    public void setDays(List<AvailableDay> days) { this.days = days; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}