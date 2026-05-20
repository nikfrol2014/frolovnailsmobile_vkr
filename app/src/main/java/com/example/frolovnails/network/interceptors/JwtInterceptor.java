package com.example.frolovnails.network.interceptors;

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

        if (token != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        return chain.proceed(request);
    }
}