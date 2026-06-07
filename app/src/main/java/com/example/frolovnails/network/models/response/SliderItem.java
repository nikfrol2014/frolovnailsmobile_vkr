package com.example.frolovnails.network.models.response;

import com.google.gson.annotations.SerializedName;

public class SliderItem {
    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("actionType")
    private String actionType;

    @SerializedName("actionValue")
    private String actionValue;

    @SerializedName("orderIndex")
    private int orderIndex;

    public SliderItem() {}

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionValue() { return actionValue; }
    public void setActionValue(String actionValue) { this.actionValue = actionValue; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}