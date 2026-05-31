package com.example.frolovnails;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.frolovnails.admin.AdminMainActivity;
import com.example.frolovnails.auth.LoginFragment;
import com.example.frolovnails.client.ClientMainActivity;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.R;

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}