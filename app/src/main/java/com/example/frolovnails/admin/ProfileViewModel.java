package com.example.frolovnails.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.request.ChangePasswordRequest;
import com.example.frolovnails.network.models.request.UpdateProfileRequest;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.ProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Resource<ProfileResponse>> profileResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> updateProfileResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> changePasswordResult = new MutableLiveData<>();

    private ProfileResponse cachedProfile;

    public ProfileViewModel(TokenManager tokenManager) {
        this.apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
    }

    public LiveData<Resource<ProfileResponse>> getProfileResult() {
        return profileResult;
    }

    public LiveData<Resource<Void>> getUpdateProfileResult() {
        return updateProfileResult;
    }

    public LiveData<Resource<Void>> getChangePasswordResult() {
        return changePasswordResult;
    }

    public ProfileResponse getProfileData() {
        return cachedProfile;
    }

    public void loadProfile() {
        profileResult.setValue(Resource.Loading.getInstance());

        apiService.getProfile().enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call, Response<ApiResponse<ProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cachedProfile = response.body().getData();
                    profileResult.setValue(new Resource.Success<>(cachedProfile));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка загрузки профиля";
                    profileResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                profileResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void updateProfile(UpdateProfileRequest request) {
        updateProfileResult.setValue(Resource.Loading.getInstance());

        apiService.updateProfile(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateProfileResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка обновления";
                    updateProfileResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                updateProfileResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }

    public void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            changePasswordResult.setValue(new Resource.Error<>("Пароли не совпадают"));
            return;
        }

        if (newPassword.length() < 6) {
            changePasswordResult.setValue(new Resource.Error<>("Пароль должен быть не менее 6 символов"));
            return;
        }

        changePasswordResult.setValue(Resource.Loading.getInstance());

        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword, confirmPassword);
        apiService.changePassword(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    changePasswordResult.setValue(new Resource.Success<>(null));
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Ошибка смены пароля";
                    changePasswordResult.setValue(new Resource.Error<>(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                changePasswordResult.setValue(new Resource.Error<>("Ошибка сети: " + t.getMessage()));
            }
        });
    }
}