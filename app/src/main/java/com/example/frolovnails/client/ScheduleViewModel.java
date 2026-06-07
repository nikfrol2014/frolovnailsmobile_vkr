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
import com.example.frolovnails.network.models.response.AvailableSlotsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel для клиентской части - работа с расписанием
 */
public class ScheduleViewModel extends ViewModel {

    private final ApiService apiService;

    // Результаты
    private final MutableLiveData<Resource<List<AvailableDay>>> availableDaysResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<AvailableSlotsResponse>> availableSlotsResult = new MutableLiveData<>();

    public ScheduleViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    // Геттеры для LiveData
    public LiveData<Resource<List<AvailableDay>>> getAvailableDaysResult() {
        return availableDaysResult;
    }

    public LiveData<Resource<AvailableSlotsResponse>> getAvailableSlotsResult() {
        return availableSlotsResult;
    }

    /**
     * Загрузить доступные дни для записи
     * @param daysCount количество дней для просмотра
     */
    public void loadAvailableDays(int daysCount) {
        availableDaysResult.setValue(Resource.Loading.getInstance());

        apiService.getAvailableDays(daysCount).enqueue(new Callback<ApiResponse<List<AvailableDay>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AvailableDay>>> call,
                                   Response<ApiResponse<List<AvailableDay>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<AvailableDay> data = response.body().getData();
                    availableDaysResult.setValue(new Resource.Success<>(data));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки доступных дней";
                    availableDaysResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AvailableDay>>> call, Throwable t) {
                availableDaysResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    /**
     * Загрузить доступные слоты для выбранной даты и услуги
     * @param date дата в формате dd.MM.yyyy
     * @param serviceId ID услуги
     */
    public void loadAvailableSlots(String date, Long serviceId) {
        availableSlotsResult.setValue(Resource.Loading.getInstance());

        apiService.getAvailableSlots(date, serviceId).enqueue(new Callback<ApiResponse<AvailableSlotsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AvailableSlotsResponse>> call,
                                   Response<ApiResponse<AvailableSlotsResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AvailableSlotsResponse data = response.body().getData();

                    // Логируем полученные слоты для отладки
                    android.util.Log.d("ScheduleViewModel", "Получены слоты: " +
                            (data != null && data.getAvailableSlots() != null ?
                                    data.getAvailableSlots().size() : 0));

                    if (data != null && data.getAvailableSlots() != null && !data.getAvailableSlots().isEmpty()) {
                        availableSlotsResult.setValue(new Resource.Success<>(data));
                    } else {
                        availableSlotsResult.setValue(new Resource.Error<>("Нет доступного времени на выбранную дату"));
                    }
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