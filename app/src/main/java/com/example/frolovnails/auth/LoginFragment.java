package com.example.frolovnails.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.frolovnails.R;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;


import android.content.Intent;
import com.example.frolovnails.admin.AdminMainActivity;
import com.example.frolovnails.client.ClientMainActivity;
import com.example.frolovnails.network.models.response.AuthResponse;

public class LoginFragment extends Fragment {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegisterLink = view.findViewById(R.id.tvRegisterLink);
        progressBar = view.findViewById(R.id.progressBar);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.login(phone, password);
        });

        tvRegisterLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_register)
        );

        authViewModel.getAuthResult().observe(getViewLifecycleOwner(), this::handleAuthResult);
    }

    private void handleAuthResult(Resource<AuthResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            AuthResponse response = ((Resource.Success<AuthResponse>) resource).getData();
            Log.d("AUTH", "Response role: '" + response.getRole() + "'");
            Log.d("AUTH", "Response accessToken: " + response.getAccessToken());

            // Сохраняем токены
            try {
                TokenManager tokenManager = new TokenManager(requireContext());
                tokenManager.saveTokens(
                        response.getAccessToken(),
                        response.getRefreshToken(),
                        response.getRole()
                );
                // Проверяем, что сохранилось
                String savedToken = tokenManager.getAccessToken();
                Log.d("AUTH", "Saved token (first 20 chars): " + (savedToken != null ? savedToken.substring(0, Math.min(20, savedToken.length())) : "null"));
                Log.d("AUTH", "Saved role: " + tokenManager.getRole());
            } catch (Exception e) {
                Log.e("AUTH", "Error saving tokens: " + e.getMessage());
            }

            Toast.makeText(getContext(), "Вход выполнен! Роль: " + response.getRole(), Toast.LENGTH_LONG).show();

            // Перенаправление в зависимости от роли
            Intent intent;
            if ("ADMIN".equals(response.getRole())) {
                intent = new Intent(getContext(), com.example.frolovnails.admin.AdminMainActivity.class);
            } else {
                intent = new Intent(getContext(), com.example.frolovnails.client.ClientMainActivity.class);
            }
            startActivity(intent);
            requireActivity().finish();

        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            String error = ((Resource.Error<AuthResponse>) resource).getMessage();
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    }
}