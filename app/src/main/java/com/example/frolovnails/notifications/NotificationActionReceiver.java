package com.example.frolovnails.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.request.UpdateAppointmentStatusRequest;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.example.frolovnails.network.models.response.Appointment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationAction";

    @Override
    public void onReceive(Context context, Intent intent) {
        long appointmentId = intent.getLongExtra("appointment_id", -1);
        String action = intent.getStringExtra("action");

        Log.d(TAG, "Action received: " + action + " for appointment " + appointmentId);

        if (appointmentId == -1) {
            Log.e(TAG, "No appointment ID");
            return;
        }

        try {
            TokenManager tokenManager = new TokenManager(context);
            ApiService apiService = ApiClient.getClient(tokenManager).create(ApiService.class);

            if ("CONFIRM".equals(action)) {
                UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
                request.setStatus("CONFIRMED");

                apiService.updateAppointmentStatus(appointmentId, request).enqueue(new Callback<ApiResponse<Appointment>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(context, "✅ Запись подтверждена", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Appointment confirmed");
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Ошибка подтверждения";
                            Toast.makeText(context, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Confirm failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                        Toast.makeText(context, "❌ Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Confirm error: " + t.getMessage());
                    }
                });

            } else if ("CANCEL".equals(action)) {
                UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
                request.setStatus("CANCELLED");

                apiService.updateAppointmentStatus(appointmentId, request).enqueue(new Callback<ApiResponse<Appointment>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(context, "❌ Запись отменена", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Appointment cancelled");
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Ошибка отмены";
                            Toast.makeText(context, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Cancel failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                        Toast.makeText(context, "❌ Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Cancel error: " + t.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            Toast.makeText(context, "❌ Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}