package com.example.frolovnails.network.models.response;

import java.io.Serializable;

public class Category implements Serializable {
    private String name;
    private int serviceCount;

    public Category() {}

    public Category(String name, int serviceCount) {
        this.name = name;
        this.serviceCount = serviceCount;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getServiceCount() { return serviceCount; }
    public void setServiceCount(int serviceCount) { this.serviceCount = serviceCount; }
}