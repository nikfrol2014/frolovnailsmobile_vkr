package com.example.frolovnails.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.request.LoginRequest;
import com.example.frolovnails.network.models.request.RegisterRequest;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.AuthResponse;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<AuthResponse>> authResult = new MutableLiveData<>();

    public AuthViewModel() {
        this.apiService = ApiClient.getClient(null).create(ApiService.class);
    }

    public LiveData<Resource<AuthResponse>> getAuthResult() {
        return authResult;
    }

    public void login(String phone, String password) {
        authResult.setValue(Resource.Loading.getInstance());

        LoginRequest request = new LoginRequest(phone, password);
        apiService.login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    authResult.setValue(new Resource.Success<>(response.body().getData()));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка входа";
                    authResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                authResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void register(String phone, String password, String firstName, String lastName) {
        authResult.setValue(Resource.Loading.getInstance());

        RegisterRequest request = new RegisterRequest(phone, password, firstName, lastName);
        apiService.register(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    authResult.setValue(new Resource.Success<>(response.body().getData()));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка регистрации";
                    authResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                authResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void resetResult() {
        authResult.setValue(null);
    }
}