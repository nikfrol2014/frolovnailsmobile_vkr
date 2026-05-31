package com.example.frolovnails.network;

import android.util.Log;

import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.AuthResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private final TokenManager tokenManager;
    private final String baseUrl;

    public TokenAuthenticator(TokenManager tokenManager, String baseUrl) {
        this.tokenManager = tokenManager;
        this.baseUrl = baseUrl;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.d(TAG, "Authenticating... Response code: " + response.code());

        // Проверяем, что это 401 и у нас есть refresh token
        if (response.code() != 401) {
            return null;
        }

        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            Log.e(TAG, "No refresh token available");
            return null;
        }

        synchronized (this) {
            // Создаем отдельный Retrofit клиент для обновления токена
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            try {
                Log.d(TAG, "Refreshing token...");
                Call<ApiResponse<AuthResponse>> call = apiService.refreshToken("Bearer " + refreshToken);
                retrofit2.Response<ApiResponse<AuthResponse>> refreshResponse = call.execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null && refreshResponse.body().isSuccess()) {
                    AuthResponse authResponse = refreshResponse.body().getData();
                    if (authResponse != null) {
                        Log.d(TAG, "Token refreshed successfully");
                        // Сохраняем новые токены
                        tokenManager.saveTokens(
                                authResponse.getAccessToken(),
                                authResponse.getRefreshToken(),
                                authResponse.getRole()
                        );
                        // Повторяем исходный запрос с новым токеном
                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + authResponse.getAccessToken())
                                .build();
                    }
                }

                Log.e(TAG, "Token refresh failed");
                // Очищаем токены и требуем повторный вход
                tokenManager.clear();
                return null;

            } catch (IOException e) {
                Log.e(TAG, "Token refresh error: " + e.getMessage());
                tokenManager.clear();
                return null;
            }
        }
    }
}