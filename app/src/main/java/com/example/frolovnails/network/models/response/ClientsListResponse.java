package com.example.frolovnails.network.models.response;

import java.util.List;

public class ClientsListResponse {
    private List<ClientListItem> clients;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;

    public ClientsListResponse() {}

    public List<ClientListItem> getClients() { return clients; }
    public void setClients(List<ClientListItem> clients) { this.clients = clients; }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}