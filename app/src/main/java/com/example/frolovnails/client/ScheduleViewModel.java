package com.example.frolovnails.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.AvailableDay;
import com.example.frolovnails.network.models.response.AvailableDaysResponse;
import com.example.frolovnails.network.models.response.AvailableSlotsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<List<AvailableDay>>> availableDaysResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<AvailableSlotsResponse>> availableSlotsResult = new MutableLiveData<>();

    public ScheduleViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<List<AvailableDay>>> getAvailableDaysResult() {
        return availableDaysResult;
    }

    public LiveData<Resource<AvailableSlotsResponse>> getAvailableSlotsResult() {
        return availableSlotsResult;
    }

    // Новый метод: загружает дни и проверяет наличие слотов для каждого
    public void loadAvailableDaysWithSlots(Long serviceId, int daysCount) {
        android.util.Log.d("ScheduleViewModel", "loadAvailableDaysWithSlots, serviceId: " + serviceId);
        availableDaysResult.setValue(Resource.Loading.getInstance());

        apiService.getAvailableDays(daysCount).enqueue(new Callback<ApiResponse<AvailableDaysResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AvailableDaysResponse>> call,
                                   Response<ApiResponse<AvailableDaysResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AvailableDaysResponse data = response.body().getData();
                    if (data != null && data.getAvailableDays() != null && !data.getAvailableDays().isEmpty()) {
                        checkSlotsForDays(data.getAvailableDays(), serviceId);
                    } else {
                        availableDaysResult.setValue(new Resource.Error<>("Нет доступных дней"));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    availableDaysResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AvailableDaysResponse>> call, Throwable t) {
                availableDaysResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    private void checkSlotsForDays(List<AvailableDay> days, Long serviceId) {
        List<AvailableDay> daysWithSlots = new ArrayList<>();
        checkNextDay(days, 0, serviceId, daysWithSlots);
    }

    private void checkNextDay(List<AvailableDay> days, int index, Long serviceId, List<AvailableDay> daysWithSlots) {
        if (index >= days.size()) {
            if (daysWithSlots.isEmpty()) {
                availableDaysResult.setValue(new Resource.Error<>("Нет дней со свободными слотами"));
            } else {
                availableDaysResult.setValue(new Resource.Success<>(daysWithSlots));
            }
            return;
        }

        AvailableDay day = days.get(index);
        apiService.getAvailableSlots(day.getAvailableDate(), serviceId).enqueue(new Callback<ApiResponse<AvailableSlotsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AvailableSlotsResponse>> call,
                                   Response<ApiResponse<AvailableSlotsResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AvailableSlotsResponse slots = response.body().getData();
                    if (slots != null && slots.getAvailableSlots() != null && !slots.getAvailableSlots().isEmpty()) {
                        daysWithSlots.add(day);
                    }
                }
                checkNextDay(days, index + 1, serviceId, daysWithSlots);
            }

            @Override
            public void onFailure(Call<ApiResponse<AvailableSlotsResponse>> call, Throwable t) {
                checkNextDay(days, index + 1, serviceId, daysWithSlots);
            }
        });
    }

    // Обычная загрузка слотов для выбранной даты
    public void loadAvailableSlots(String date, Long serviceId) {
        availableSlotsResult.setValue(Resource.Loading.getInstance());

        apiService.getAvailableSlots(date, serviceId).enqueue(new Callback<ApiResponse<AvailableSlotsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AvailableSlotsResponse>> call,
                                   Response<ApiResponse<AvailableSlotsResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AvailableSlotsResponse data = response.body().getData();
                    availableSlotsResult.setValue(new Resource.Success<>(data));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки слотов";
                    availableSlotsResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AvailableSlotsResponse>> call, Throwable t) {
                availableSlotsResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}