package com.example.frolovnails.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.interceptors.JwtInterceptor;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.0.111:8080/";
    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;

    public static Retrofit getClient(TokenManager tokenManager) {
        if (retrofit == null || okHttpClient == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    // Логирование
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // Строитель OkHttpClient
                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS);

                    // Добавляем JWT интерцептор
                    if (tokenManager != null) {
                        httpClient.addInterceptor(new JwtInterceptor(tokenManager));
                        // Добавляем Authenticator для автоматического обновления токена
                        httpClient.authenticator(new TokenAuthenticator(tokenManager, BASE_URL));
                    }

                    okHttpClient = httpClient.build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .build();
                }
            }
        }
        return retrofit;
    }
}