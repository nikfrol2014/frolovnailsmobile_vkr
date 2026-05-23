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
import com.example.frolovnails.network.models.response.ClientsListResponse;
import com.example.frolovnails.network.models.request.UpdateClientRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<ClientsListResponse>> clientsResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<ClientDetailsResponse>> clientDetailsResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> updateClientResult = new MutableLiveData<>();

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
                        clientsResult.setValue(new Resource.Success<>(data));
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