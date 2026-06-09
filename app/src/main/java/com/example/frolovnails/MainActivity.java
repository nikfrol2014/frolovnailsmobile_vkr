package com.example.frolovnails;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.frolovnails.admin.AdminMainActivity;
import com.example.frolovnails.client.ClientMainActivity;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.R;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            tokenManager = new TokenManager(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Проверяем авторизацию
        if (tokenManager != null && tokenManager.isLoggedIn()) {
            String role = tokenManager.getRole();
            if ("ADMIN".equals(role)) {
                startActivity(new Intent(this, AdminMainActivity.class));
                finish();
                return;
            } else if ("CLIENT".equals(role)) {
                startActivity(new Intent(this, ClientMainActivity.class));
                finish();
                return;
            }
        }

        // Если не авторизован - показываем LoginFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // ========== ИНИЦИАЛИЗАЦИЯ FCM ==========
        initFcm();
    }

    private void initFcm() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d("FCM", "Firebase token: " + token);
                        saveFcmToken(token);
                    } else {
                        Log.e("FCM", "Failed to get FCM token", task.getException());
                    }
                });
    }

    private void saveFcmToken(String fcmToken) {
        if (tokenManager == null || !tokenManager.isLoggedIn()) {
            return;
        }

        try {
            ApiService apiService = ApiClient.getClient(tokenManager).create(ApiService.class);
            Map<String, String> request = new HashMap<>();
            request.put("token", fcmToken);

            apiService.saveFcmToken(request).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        Log.d("FCM", "FCM token saved on server");
                    } else {
                        Log.e("FCM", "Failed to save token: " + response.code());
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("FCM", "Error saving token: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("FCM", "Error: " + e.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void setAppTheme(int themeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode);
        recreate();
    }
}