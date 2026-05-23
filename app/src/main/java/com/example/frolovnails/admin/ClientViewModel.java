package com.example.frolovnails.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.ClientDetailsResponse;
import com.example.frolovnails.network.models.response.ClientListItem;
import com.example.frolovnails.network.models.response.ClientsListResponse;
import com.example.frolovnails.network.models.request.UpdateClientRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<ClientsListResponse>> clientsResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<ClientDetailsResponse>> clientDetailsResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> updateClientResult = new MutableLiveData<>();

    // Для сортировки и поиска на клиенте
    private List<ClientListItem> allClients = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentSortType = "name"; // name, visits, spent

    public ClientViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<ClientsListResponse>> getClientsResult() {
        return clientsResult;
    }

    public LiveData<Resource<ClientDetailsResponse>> getClientDetailsResult() {
        return clientDetailsResult;
    }

    public LiveData<Resource<Void>> getUpdateClientResult() {
        return updateClientResult;
    }

    public void loadClients(int page, int size, String search) {
        clientsResult.setValue(Resource.Loading.getInstance());

        apiService.getClients(page, size, search).enqueue(new Callback<ApiResponse<ClientsListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ClientsListResponse>> call, Response<ApiResponse<ClientsListResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ClientsListResponse data = response.body().getData();
                    if (data != null) {
                        allClients = data.getClients() != null ? data.getClients() : new ArrayList<>();
                        applyFiltersAndSort();
                    } else {
                        clientsResult.setValue(new Resource.Error<>("Нет данных"));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    clientsResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ClientsListResponse>> call, Throwable t) {
                clientsResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Поиск (фильтрация на клиенте)
    public void search(String query) {
        this.currentSearchQuery = query;
        applyFiltersAndSort();
    }

    // Сортировка
    public void sortBy(String sortType) {
        this.currentSortType = sortType;
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        List<ClientListItem> filtered = new ArrayList<>(allClients);

        // Фильтрация по поисковому запросу
        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            String lowerQuery = currentSearchQuery.toLowerCase();
            filtered.removeIf(client ->
                    (client.getFirstName() == null || !client.getFirstName().toLowerCase().contains(lowerQuery)) &&
                            (client.getLastName() == null || !client.getLastName().toLowerCase().contains(lowerQuery)) &&
                            (client.getPhone() == null || !client.getPhone().toLowerCase().contains(lowerQuery))
            );
        }

        // Сортировка
        switch (currentSortType) {
            case "name":
                filtered.sort((a, b) -> {
                    String nameA = (a.getFirstName() != null ? a.getFirstName() : "") + " " + (a.getLastName() != null ? a.getLastName() : "");
                    String nameB = (b.getFirstName() != null ? b.getFirstName() : "") + " " + (b.getLastName() != null ? b.getLastName() : "");
                    return nameA.compareToIgnoreCase(nameB);
                });
                break;
            case "visits":
                filtered.sort((a, b) -> {
                    int visitsA = a.getTotalVisits() != null ? a.getTotalVisits() : 0;
                    int visitsB = b.getTotalVisits() != null ? b.getTotalVisits() : 0;
                    return Integer.compare(visitsB, visitsA); // по убыванию
                });
                break;
            case "spent":
                // Для spent нужно загружать с сервера, пока сортируем по визитам
                filtered.sort((a, b) -> {
                    int visitsA = a.getTotalVisits() != null ? a.getTotalVisits() : 0;
                    int visitsB = b.getTotalVisits() != null ? b.getTotalVisits() : 0;
                    return Integer.compare(visitsB, visitsA);
                });
                break;
        }

        ClientsListResponse response = new ClientsListResponse();
        response.setClients(filtered);
        response.setTotal((long) filtered.size());
        response.setPage(0);
        response.setSize(filtered.size());
        response.setTotalPages(1);
        clientsResult.setValue(new Resource.Success<>(response));
    }

    public void loadClientDetails(Long clientId) {
        clientDetailsResult.setValue(Resource.Loading.getInstance());

        apiService.getClientDetails(clientId).enqueue(new Callback<ApiResponse<ClientDetailsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ClientDetailsResponse>> call, Response<ApiResponse<ClientDetailsResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ClientDetailsResponse data = response.body().getData();
                    if (data != null) {
                        clientDetailsResult.setValue(new Resource.Success<>(data));
                    } else {
                        clientDetailsResult.setValue(new Resource.Error<>("Нет данных"));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    clientDetailsResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ClientDetailsResponse>> call, Throwable t) {
                clientDetailsResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void updateClient(Long clientId, UpdateClientRequest request) {
        updateClientResult.setValue(Resource.Loading.getInstance());

        apiService.updateClient(clientId, request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateClientResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка обновления";
                    updateClientResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                updateClientResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}