package com.example.frolovnails.network.models.request;

import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateAppointmentStatusRequest implements Serializable {
    private String status;
    private String masterNotes;

    // Новые поля для фактических данных при завершении
    private BigDecimal actualPrice;
    private String actualServices;
    private String masterCompletionComment;

    public UpdateAppointmentStatusRequest() {}

    public UpdateAppointmentStatusRequest(String status, String masterNotes) {
        this.status = status;
        this.masterNotes = masterNotes;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMasterNotes() { return masterNotes; }
    public void setMasterNotes(String masterNotes) { this.masterNotes = masterNotes; }

    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }

    public String getActualServices() { return actualServices; }
    public void setActualServices(String actualServices) { this.actualServices = actualServices; }

    public String getMasterCompletionComment() { return masterCompletionComment; }
    public void setMasterCompletionComment(String masterCompletionComment) { this.masterCompletionComment = masterCompletionComment; }
}