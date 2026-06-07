package com.example.frolovnails.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.AppointmentsListResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAppointmentsViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<List<Appointment>>> appointmentsResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> cancelResult = new MutableLiveData<>();

    private List<Appointment> appointmentsList;

    public MyAppointmentsViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<List<Appointment>>> getAppointmentsResult() {
        return appointmentsResult;
    }

    public LiveData<Resource<Void>> getCancelResult() {
        return cancelResult;
    }

    public List<Appointment> getAppointmentsList() {
        return appointmentsList;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentsList = appointments;
    }

    public void loadMyAppointments() {
        appointmentsResult.setValue(Resource.Loading.getInstance());

        // Загружаем все записи без ограничений
        apiService.getMyAppointmentsFiltered(null, null, null, 0, 100)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                           Response<ApiResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            if (data != null && data.containsKey("appointments")) {
                                Object appointmentsObj = data.get("appointments");
                                Gson gson = new Gson();
                                String json = gson.toJson(appointmentsObj);
                                Type type = new TypeToken<List<Appointment>>(){}.getType();
                                List<Appointment> appointments = gson.fromJson(json, type);
                                appointmentsResult.setValue(new Resource.Success<>(appointments));
                            } else {
                                appointmentsResult.setValue(new Resource.Success<>(List.of()));
                            }
                        } else {
                            String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                            appointmentsResult.setValue(new Resource.Error<>(msg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        appointmentsResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
                    }
                });
    }

    public void cancelAppointment(Long appointmentId) {
        cancelResult.setValue(Resource.Loading.getInstance());

        apiService.cancelAppointment(appointmentId).enqueue(new Callback<ApiResponse<Appointment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cancelResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка отмены";
                    cancelResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                cancelResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}