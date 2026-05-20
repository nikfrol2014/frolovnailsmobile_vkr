package com.example.frolovnails.network;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.interceptors.JwtInterceptor;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.0.111:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(TokenManager tokenManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        if (tokenManager != null) {
            JwtInterceptor interceptor = new JwtInterceptor(tokenManager);
            httpClient.addInterceptor(interceptor);
            Log.d("ApiClient", "JwtInterceptor added");
        } else {
            Log.d("ApiClient", "TokenManager is null, no interceptor");
        }

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
    }
}