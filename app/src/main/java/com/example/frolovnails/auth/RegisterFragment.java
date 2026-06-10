package com.example.frolovnails.auth;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.frolovnails.utils.ToastUtils;

import java.util.Calendar;
import java.util.Locale;


public class RegisterFragment extends Fragment {

    private EditText etFirstName, etLastName, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private DatePickerDialog datePickerDialog;
    private EditText etBirthDate, etNotes;

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

        etBirthDate = view.findViewById(R.id.etBirthDate);
        etBirthDate.setInputType(InputType.TYPE_NULL);
        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();  // ← добавить

            if (firstName.isEmpty() || phone.isEmpty() || password.isEmpty() || birthDate.isEmpty()) {
                ToastUtils.show(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT);
                return;
            }

            if (!password.equals(confirmPassword)) {
                ToastUtils.show(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT);
                return;
            }

            authViewModel.register(phone, password, firstName, lastName, birthDate);
        });

        tvLoginLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_register_to_login)
        );

        authViewModel.getAuthResult().observe(getViewLifecycleOwner(), this::handleAuthResult);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    // Формат: dd.MM.yyyy (как ожидает сервер)
                    String selectedDate = String.format(Locale.getDefault(),
                            "%02d.%02d.%04d", dayOfMonth, month + 1, year);
                    etBirthDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
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
            ToastUtils.show(getContext(), "Регистрация успешна! Теперь войдите.", Toast.LENGTH_LONG);

            Navigation.findNavController(requireView()).navigate(R.id.action_register_to_login);

        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            String error = ((Resource.Error<AuthResponse>) resource).getMessage();
            ToastUtils.show(getContext(), error, Toast.LENGTH_SHORT);
        }
    }
}