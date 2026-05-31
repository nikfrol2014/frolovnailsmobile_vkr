package com.example.frolovnails.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.stats.DashboardStatsResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<DashboardStatsResponse>> dashboardStats = new MutableLiveData<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public StatsViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<DashboardStatsResponse>> getDashboardStats() {
        return dashboardStats;
    }

    public void loadStatsForWeek() {
        Calendar calendar = Calendar.getInstance();

        // Начало недели (понедельник)
        Calendar startCal = (Calendar) calendar.clone();
        startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String startDate = dateFormat.format(startCal.getTime());

        // Конец недели (воскресенье)
        Calendar endCal = (Calendar) calendar.clone();
        endCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String endDate = dateFormat.format(endCal.getTime());

        loadStats(startDate, endDate);
    }

    public void loadStatsForMonth() {
        Calendar calendar = Calendar.getInstance();

        // Начало месяца
        Calendar startCal = (Calendar) calendar.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(startCal.getTime());

        // Конец месяца
        Calendar endCal = (Calendar) calendar.clone();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(endCal.getTime());

        loadStats(startDate, endDate);
    }

    public void loadStatsForPeriod(String startDate, String endDate) {
        loadStats(startDate, endDate);
    }

    private void loadStats(String startDate, String endDate) {
        dashboardStats.setValue(Resource.Loading.getInstance());

        apiService.getDashboardStats(startDate, endDate).enqueue(new Callback<ApiResponse<DashboardStatsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardStatsResponse>> call,
                                   Response<ApiResponse<DashboardStatsResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DashboardStatsResponse data = response.body().getData();
                    if (data != null) {
                        dashboardStats.setValue(new Resource.Success<>(data));
                    } else {
                        dashboardStats.setValue(new Resource.Error<>("Нет данных"));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    dashboardStats.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardStatsResponse>> call, Throwable t) {
                dashboardStats.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void refresh() {
        loadStatsForWeek();
    }
}