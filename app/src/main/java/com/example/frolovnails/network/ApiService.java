package com.example.frolovnails.network;

import retrofit2.Call;
import retrofit2.http.*;
import com.example.frolovnails.network.models.*;

public interface ApiService {
    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
}