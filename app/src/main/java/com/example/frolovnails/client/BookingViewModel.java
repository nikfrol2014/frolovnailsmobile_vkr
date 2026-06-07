package com.example.frolovnails.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.request.CreateAppointmentRequest;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.Appointment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<Void>> createAppointmentResult = new MutableLiveData<>();

    public BookingViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<Void>> getCreateAppointmentResult() {
        return createAppointmentResult;
    }

    public void createAppointment(CreateAppointmentRequest request) {
        createAppointmentResult.setValue(Resource.Loading.getInstance());

        apiService.createClientAppointment(request).enqueue(new Callback<ApiResponse<Appointment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    createAppointmentResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка создания записи";
                    createAppointmentResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                createAppointmentResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}