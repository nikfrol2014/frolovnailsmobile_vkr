package com.example.frolovnails.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frolovnails.R;
import com.example.frolovnails.admin.ProfileViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.utils.ToastUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ChangePasswordDialog extends DialogFragment {

    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnSave, btnCancel;
    private View progressBar;
    private ProfileViewModel viewModel;
    private OnPasswordChangedListener listener;

    public interface OnPasswordChangedListener {
        void onPasswordChanged();
    }

    public static ChangePasswordDialog newInstance() {
        return new ChangePasswordDialog();
    }

    public void setOnPasswordChangedListener(OnPasswordChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Смена пароля");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        TokenManager tokenManager = null;
        try {
            tokenManager = new TokenManager(requireContext());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        final TokenManager finalTokenManager = tokenManager;

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ProfileViewModel(finalTokenManager);
            }
        }).get(ProfileViewModel.class);

        btnSave.setOnClickListener(v -> changePassword());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void changePassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty()) {
            ToastUtils.show(getContext(), "Введите старый пароль", Toast.LENGTH_SHORT);
            return;
        }

        if (newPassword.isEmpty()) {
            ToastUtils.show(getContext(), "Введите новый пароль", Toast.LENGTH_SHORT);
            return;
        }

        viewModel.changePassword(oldPassword, newPassword, confirmPassword);
        viewModel.getChangePasswordResult().observe(getViewLifecycleOwner(), this::handleResult);
    }

    private void handleResult(Resource<Void> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "✅ Пароль изменен", Toast.LENGTH_SHORT);
            if (listener != null) {
                listener.onPasswordChanged();
            }
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<Void>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }
}