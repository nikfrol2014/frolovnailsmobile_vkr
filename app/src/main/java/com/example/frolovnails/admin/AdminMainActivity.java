package com.example.frolovnails.admin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.frolovnails.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavViewAdmin);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_admin);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Обрабатываем выбор пунктов меню вручную
            bottomNavView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                // Очищаем стек навигации при переходе между основными разделами
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph_admin, true)
                        .build();

                if (itemId == R.id.nav_calendar) {
                    navController.navigate(R.id.nav_calendar, null, navOptions);
                    return true;
                } else if (itemId == R.id.nav_stats) {
                    navController.navigate(R.id.nav_stats, null, navOptions);
                    return true;
                } else if (itemId == R.id.nav_clients) {
                    navController.navigate(R.id.nav_clients, null, navOptions);
                    return true;
                } else if (itemId == R.id.nav_services_admin) {
                    navController.navigate(R.id.nav_services_admin, null, navOptions);
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    navController.navigate(R.id.nav_settings, null, navOptions);
                    return true;
                }
                return false;
            });
        }
    }
}