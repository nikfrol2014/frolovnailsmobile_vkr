package com.example.frolovnails.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.TimelineResponse;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<TimelineResponse>> timelineResult = new MutableLiveData<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public CalendarViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<TimelineResponse>> getTimelineResult() {
        return timelineResult;
    }

    public void loadTimeline(int daysCount) {
        timelineResult.setValue(Resource.Loading.getInstance());

        Calendar cal = Calendar.getInstance();
        String startDate = dateFormat.format(cal.getTime());

        apiService.getTimeline(startDate, daysCount).enqueue(new Callback<ApiResponse<TimelineResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TimelineResponse>> call, Response<ApiResponse<TimelineResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    TimelineResponse data = response.body().getData();
                    if (data != null) {
                        timelineResult.setValue(new Resource.Success<>(data));
                    } else {
                        timelineResult.setValue(new Resource.Error<>("Нет данных"));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    timelineResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TimelineResponse>> call, Throwable t) {
                timelineResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void refresh() {
        loadTimeline(7);
    }
}