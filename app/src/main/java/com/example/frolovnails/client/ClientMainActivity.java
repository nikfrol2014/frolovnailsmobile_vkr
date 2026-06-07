package com.example.frolovnails.client;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.frolovnails.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClientMainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_client);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Ручная обработка кликов
            bottomNavView.setOnItemSelectedListener(this::onNavigationItemSelected);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Очищаем стек навигации при переключении
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                .build();

        if (itemId == R.id.nav_home) {
            navController.navigate(R.id.nav_home, null, navOptions);
            return true;
        } else if (itemId == R.id.nav_booking) {
            navController.navigate(R.id.nav_booking, null, navOptions);
            return true;
        } else if (itemId == R.id.nav_my_appointments) {
            navController.navigate(R.id.nav_my_appointments, null, navOptions);
            return true;
        } else if (itemId == R.id.nav_profile) {
            navController.navigate(R.id.nav_profile, null, navOptions);
            return true;
        }

        return false;
    }
}