package com.example.frolovnails.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.request.ServiceRequest;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.Service;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesAdminViewModel extends ViewModel {

    private final ApiService apiService;

    private final MutableLiveData<Resource<List<Service>>> servicesResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Service>> createResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Service>> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Service>> activateResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> deactivateResult = new MutableLiveData<>();

    public ServicesAdminViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    // Getters для LiveData
    public LiveData<Resource<List<Service>>> getServicesResult() { return servicesResult; }
    public LiveData<Resource<Service>> getCreateResult() { return createResult; }
    public LiveData<Resource<Service>> getUpdateResult() { return updateResult; }
    public LiveData<Resource<Service>> getActivateResult() { return activateResult; }
    public LiveData<Resource<Void>> getDeactivateResult() { return deactivateResult; }

    // Загрузить все услуги (активные и неактивные)
    public void loadAllServices() {
        servicesResult.setValue(Resource.Loading.getInstance());

        apiService.getAllServices().enqueue(new Callback<ApiResponse<List<Service>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Service>>> call, Response<ApiResponse<List<Service>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Service> data = response.body().getData();
                    if (data != null) {
                        servicesResult.setValue(new Resource.Success<>(data));
                    } else {
                        servicesResult.setValue(new Resource.Error<>("Нет данных"));
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

    // Создать услугу
    public void createService(ServiceRequest request) {
        createResult.setValue(Resource.Loading.getInstance());

        apiService.createService(request).enqueue(new Callback<ApiResponse<Service>>() {
            @Override
            public void onResponse(Call<ApiResponse<Service>> call, Response<ApiResponse<Service>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Service data = response.body().getData();
                    createResult.setValue(data != null ? new Resource.Success<>(data) : new Resource.Error<>("Ошибка создания"));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка создания";
                    createResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Service>> call, Throwable t) {
                createResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Обновить услугу (PUT)
    public void updateService(Long id, ServiceRequest request) {
        updateResult.setValue(Resource.Loading.getInstance());

        apiService.updateService(id, request).enqueue(new Callback<ApiResponse<Service>>() {
            @Override
            public void onResponse(Call<ApiResponse<Service>> call, Response<ApiResponse<Service>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Service data = response.body().getData();
                    updateResult.setValue(data != null ? new Resource.Success<>(data) : new Resource.Error<>("Ошибка обновления"));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка обновления";
                    updateResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Service>> call, Throwable t) {
                updateResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Активировать услугу (PATCH)
    public void activateService(Long id) {
        activateResult.setValue(Resource.Loading.getInstance());

        apiService.activateService(id).enqueue(new Callback<ApiResponse<Service>>() {
            @Override
            public void onResponse(Call<ApiResponse<Service>> call, Response<ApiResponse<Service>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Сервер возвращает data = null, но операция успешна
                    // Поэтому просто передаём success без данных
                    activateResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка активации";
                    activateResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Service>> call, Throwable t) {
                activateResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Деактивировать услугу (DELETE)
    public void deactivateService(Long id) {
        deactivateResult.setValue(Resource.Loading.getInstance());

        apiService.deactivateService(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    deactivateResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка деактивации";
                    deactivateResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                deactivateResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}