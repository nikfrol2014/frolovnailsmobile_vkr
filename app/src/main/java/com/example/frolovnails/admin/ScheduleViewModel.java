package com.example.frolovnails.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.request.CreateAvailableDayRequest;
import com.example.frolovnails.network.models.request.CreateScheduleBlockRequest;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.AvailableDay;
import com.example.frolovnails.network.models.response.AvailableDaysResponse;
import com.example.frolovnails.network.models.response.ScheduleBlock;
import com.example.frolovnails.network.models.response.ScheduleBlocksResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<List<AvailableDay>>> availableDaysResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<ScheduleBlock>>> scheduleBlocksResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<ScheduleBlocksResponse>> scheduleBlocksResponseResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<AvailableDay>> addAvailableDayResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<AvailableDay>> updateAvailableDayResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> deleteAvailableDayResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<ScheduleBlock>> addScheduleBlockResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> deleteScheduleBlockResult = new MutableLiveData<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public ScheduleViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public ScheduleViewModel() {
        this.apiService = ApiClient.getClient(null).create(ApiService.class);
    }

    // Getters
    public LiveData<Resource<List<AvailableDay>>> getAvailableDaysResult() { return availableDaysResult; }
    public LiveData<Resource<List<ScheduleBlock>>> getScheduleBlocksResult() { return scheduleBlocksResult; }
    public LiveData<Resource<ScheduleBlocksResponse>> getScheduleBlocksResponseResult() { return scheduleBlocksResponseResult; }
    public LiveData<Resource<AvailableDay>> getAddAvailableDayResult() { return addAvailableDayResult; }
    public LiveData<Resource<AvailableDay>> getUpdateAvailableDayResult() { return updateAvailableDayResult; }
    public LiveData<Resource<Void>> getDeleteAvailableDayResult() { return deleteAvailableDayResult; }
    public LiveData<Resource<ScheduleBlock>> getAddScheduleBlockResult() { return addScheduleBlockResult; }
    public LiveData<Resource<Void>> getDeleteScheduleBlockResult() { return deleteScheduleBlockResult; }

    // Загрузить доступные дни
    public void loadAvailableDays(int monthsCount) {
        availableDaysResult.setValue(Resource.Loading.getInstance());

        Calendar cal = Calendar.getInstance();
        String startDate = dateFormat.format(cal.getTime());
        cal.add(Calendar.MONTH, monthsCount);
        String endDate = dateFormat.format(cal.getTime());

        apiService.getAvailableDaysForAdmin(startDate, endDate).enqueue(new Callback<ApiResponse<AvailableDaysResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AvailableDaysResponse>> call, Response<ApiResponse<AvailableDaysResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AvailableDaysResponse data = response.body().getData();
                    if (data != null && data.getDays() != null) {
                        availableDaysResult.setValue(new Resource.Success<>(data.getDays()));
                    } else {
                        availableDaysResult.setValue(new Resource.Error<>("Нет данных"));
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

    // Загрузить блокировки
    public void loadScheduleBlocks(int monthsCount) {
        scheduleBlocksResult.setValue(Resource.Loading.getInstance());

        Calendar cal = Calendar.getInstance();
        String startDate = dateFormat.format(cal.getTime());
        cal.add(Calendar.MONTH, monthsCount);
        String endDate = dateFormat.format(cal.getTime());

        apiService.getScheduleBlocks(startDate, endDate).enqueue(new Callback<ApiResponse<ScheduleBlocksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ScheduleBlocksResponse>> call, Response<ApiResponse<ScheduleBlocksResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ScheduleBlocksResponse data = response.body().getData();
                    if (data != null && data.getBlocks() != null) {
                        scheduleBlocksResult.setValue(new Resource.Success<>(data.getBlocks()));
                    } else {
                        scheduleBlocksResult.setValue(new Resource.Error<>("Нет данных"));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    scheduleBlocksResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ScheduleBlocksResponse>> call, Throwable t) {
                scheduleBlocksResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Получить блокировки для диапазона дат (возвращает ScheduleBlocksResponse)
    public void getScheduleBlocks(String startDate, String endDate) {
        scheduleBlocksResponseResult.setValue(Resource.Loading.getInstance());

        apiService.getScheduleBlocks(startDate, endDate).enqueue(new Callback<ApiResponse<ScheduleBlocksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ScheduleBlocksResponse>> call, Response<ApiResponse<ScheduleBlocksResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ScheduleBlocksResponse data = response.body().getData();
                    scheduleBlocksResponseResult.setValue(data != null ? new Resource.Success<>(data) : new Resource.Error<>("Нет данных"));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    scheduleBlocksResponseResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ScheduleBlocksResponse>> call, Throwable t) {
                scheduleBlocksResponseResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Добавить доступный день
    public void addAvailableDay(String date, String workStart, String workEnd, String notes) {
        addAvailableDayResult.setValue(Resource.Loading.getInstance());
        apiService.addAvailableDay(date, workStart, workEnd, notes).enqueue(new Callback<ApiResponse<AvailableDay>>() {
            @Override
            public void onResponse(Call<ApiResponse<AvailableDay>> call, Response<ApiResponse<AvailableDay>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AvailableDay data = response.body().getData();
                    addAvailableDayResult.setValue(data != null ? new Resource.Success<>(data) : new Resource.Error<>("Ошибка добавления"));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка добавления";
                    addAvailableDayResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AvailableDay>> call, Throwable t) {
                addAvailableDayResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Обновить доступный день
    public void updateAvailableDay(Long id, String workStart, String workEnd, Boolean isAvailable, String notes) {
        updateAvailableDayResult.setValue(Resource.Loading.getInstance());
        apiService.updateAvailableDay(id, workStart, workEnd, isAvailable, notes).enqueue(new Callback<ApiResponse<AvailableDay>>() {
            @Override
            public void onResponse(Call<ApiResponse<AvailableDay>> call, Response<ApiResponse<AvailableDay>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AvailableDay data = response.body().getData();
                    updateAvailableDayResult.setValue(data != null ? new Resource.Success<>(data) : new Resource.Error<>("Ошибка обновления"));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка обновления";
                    updateAvailableDayResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AvailableDay>> call, Throwable t) {
                updateAvailableDayResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Удалить доступный день
    public void deleteAvailableDay(Long id) {
        deleteAvailableDayResult.setValue(Resource.Loading.getInstance());
        apiService.deleteAvailableDay(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    deleteAvailableDayResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка удаления";
                    deleteAvailableDayResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                deleteAvailableDayResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Добавить блокировку
    public void addScheduleBlock(CreateScheduleBlockRequest request) {
        addScheduleBlockResult.setValue(Resource.Loading.getInstance());
        apiService.addScheduleBlock(request).enqueue(new Callback<ApiResponse<ScheduleBlock>>() {
            @Override
            public void onResponse(Call<ApiResponse<ScheduleBlock>> call, Response<ApiResponse<ScheduleBlock>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ScheduleBlock data = response.body().getData();
                    addScheduleBlockResult.setValue(data != null ? new Resource.Success<>(data) : new Resource.Error<>("Ошибка добавления"));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка добавления";
                    addScheduleBlockResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ScheduleBlock>> call, Throwable t) {
                addScheduleBlockResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    // Удалить блокировку
    public void deleteScheduleBlock(Long id) {
        deleteScheduleBlockResult.setValue(Resource.Loading.getInstance());
        apiService.deleteScheduleBlock(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    deleteScheduleBlockResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка удаления";
                    deleteScheduleBlockResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                deleteScheduleBlockResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}