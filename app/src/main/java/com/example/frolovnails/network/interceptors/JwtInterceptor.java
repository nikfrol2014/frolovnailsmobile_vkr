package com.example.frolovnails.network.interceptors;

import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import com.example.frolovnails.common.TokenManager;
import java.io.IOException;

public class JwtInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public JwtInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = tokenManager.getAccessToken();
        Request request = chain.request();

        Log.d("JWT", "=== JWT Interceptor called ===");
        Log.d("JWT", "Request URL: " + request.url());
        Log.d("JWT", "Token from manager: " + (token != null ? "present, length=" + token.length() : "null"));

        if (token != null && !token.isEmpty()) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            Log.d("JWT", "✅ Authorization header added");
        } else {
            Log.w("JWT", "❌ No token available");
        }

        return chain.proceed(request);
    }
}