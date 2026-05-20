package com.example.frolovnails.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.Service;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<List<Service>>> servicesResult = new MutableLiveData<>();

    public ServicesViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<List<Service>>> getServicesResult() {
        return servicesResult;
    }

    public void loadServices() {
        servicesResult.setValue(Resource.Loading.getInstance());

        apiService.getServices().enqueue(new Callback<ApiResponse<List<Service>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Service>>> call, Response<ApiResponse<List<Service>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Service> data = response.body().getData();
                    if (data != null) {
                        servicesResult.setValue(new Resource.Success<>(data));
                    } else {
                        servicesResult.setValue(new Resource.Error<>("Нет услуг"));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    servicesResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Service>>> call, Throwable t) {
                servicesResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}