package com.example.frolovnails.auth;

import android.os.Bundle;
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
import com.example.frolovnails.network.models.response.AuthResponse;


public class RegisterFragment extends Fragment {

    private EditText etFirstName, etLastName, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvLoginLink = view.findViewById(R.id.tvLoginLink);
        progressBar = view.findViewById(R.id.progressBar);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (firstName.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.register(phone, password, firstName, lastName);
        });

        tvLoginLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_register_to_login)
        );

        authViewModel.getAuthResult().observe(getViewLifecycleOwner(), this::handleAuthResult);
    }

    private void handleAuthResult(Resource<AuthResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);

            AuthResponse response = ((Resource.Success<AuthResponse>) resource).getData();
            Toast.makeText(getContext(), "Регистрация успешна! Теперь войдите.", Toast.LENGTH_LONG).show();

            Navigation.findNavController(requireView()).navigate(R.id.action_register_to_login);

        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            String error = ((Resource.Error<AuthResponse>) resource).getMessage();
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    }
}