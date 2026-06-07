package com.example.frolovnails.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.SliderItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContentViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<List<SliderItem>>> sliderResult = new MutableLiveData<>();

    public ContentViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<List<SliderItem>>> getSliderResult() {
        return sliderResult;
    }

    public void loadSliderItems() {
        sliderResult.setValue(Resource.Loading.getInstance());

        apiService.getSliderItems().enqueue(new Callback<ApiResponse<List<SliderItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SliderItem>>> call,
                                   Response<ApiResponse<List<SliderItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SliderItem> data = response.body().getData();
                    sliderResult.setValue(new Resource.Success<>(data));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки";
                    sliderResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SliderItem>>> call, Throwable t) {
                sliderResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}