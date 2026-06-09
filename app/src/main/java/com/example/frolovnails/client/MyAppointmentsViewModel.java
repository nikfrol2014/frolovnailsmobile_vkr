package com.example.frolovnails.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.request.UpdateAppointmentStatusRequest;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.AppointmentsListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAppointmentsViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<List<Appointment>>> appointmentsResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Appointment>> updateStatusResult = new MutableLiveData<>();

    private List<Appointment> appointmentsList;

    public MyAppointmentsViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<List<Appointment>>> getAppointmentsResult() {
        return appointmentsResult;
    }

    public LiveData<Resource<Appointment>> getUpdateStatusResult() {
        return updateStatusResult;
    }

    public List<Appointment> getAppointmentsList() {
        return appointmentsList;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentsList = appointments;
    }

    public void loadMyAppointments() {
        appointmentsResult.setValue(Resource.Loading.getInstance());

        apiService.getMyAppointments().enqueue(new Callback<ApiResponse<AppointmentsListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AppointmentsListResponse>> call,
                                   Response<ApiResponse<AppointmentsListResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AppointmentsListResponse data = response.body().getData();
                    if (data != null && data.getAppointments() != null) {
                        appointmentsResult.setValue(new Resource.Success<>(data.getAppointments()));
                    } else {
                        appointmentsResult.setValue(new Resource.Success<>(List.of()));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    appointmentsResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AppointmentsListResponse>> call, Throwable t) {
                appointmentsResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void updateAppointmentStatus(Long appointmentId, UpdateAppointmentStatusRequest request) {
        updateStatusResult.setValue(Resource.Loading.getInstance());

        apiService.updateClientAppointmentStatus(appointmentId, request).enqueue(new Callback<ApiResponse<Appointment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Appointment data = response.body().getData();
                    updateStatusResult.setValue(new Resource.Success<>(data));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка обновления статуса";
                    updateStatusResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                updateStatusResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}