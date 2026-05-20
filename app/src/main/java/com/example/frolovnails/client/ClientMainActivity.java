package com.example.frolovnails.client;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
//import androidx.navigation.ui.NavigationUI;

import com.example.frolovnails.R;
import com.example.frolovnails.databinding.ActivityClientMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClientMainActivity extends AppCompatActivity {

    private ActivityClientMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClientMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_client);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavView = binding.bottomNavView;
//            NavigationUI.setupWithNavController(bottomNavView, navController);
        }
    }
}