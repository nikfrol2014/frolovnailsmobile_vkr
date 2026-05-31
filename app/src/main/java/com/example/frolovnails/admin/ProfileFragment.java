package com.example.frolovnails.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frolovnails.MainActivity;
import com.example.frolovnails.R;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.ProfileResponse;
import com.example.frolovnails.ui.ChangePasswordDialog;
import com.example.frolovnails.ui.EditProfileDialog;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvPhone, tvBirthDate, tvRole, tvRegisteredAt;
    private MaterialCardView cardEditProfile, cardChangePassword;
    private Button btnLogout;
    private View progressBar;
    private ProfileViewModel viewModel;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupClickListeners();
        loadProfile();
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvRole = view.findViewById(R.id.tvRole);
        tvRegisteredAt = view.findViewById(R.id.tvRegisteredAt);
        cardEditProfile = view.findViewById(R.id.cardEditProfile);
        cardChangePassword = view.findViewById(R.id.cardChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void initViewModel() {
        try {
            tokenManager = new TokenManager(requireContext());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ProfileViewModel(tokenManager);
            }
        }).get(ProfileViewModel.class);

        viewModel.getProfileResult().observe(getViewLifecycleOwner(), this::handleProfileResult);
        viewModel.getUpdateProfileResult().observe(getViewLifecycleOwner(), this::handleUpdateResult);
        viewModel.getChangePasswordResult().observe(getViewLifecycleOwner(), this::handlePasswordResult);
    }

    private void setupClickListeners() {
        cardEditProfile.setOnClickListener(v -> {
            if (viewModel.getProfileData() != null) {
                EditProfileDialog dialog = EditProfileDialog.newInstance(viewModel.getProfileData());
                dialog.setOnSaveListener(() -> loadProfile());
                dialog.show(getChildFragmentManager(), "edit_profile");
            }
        });

        cardChangePassword.setOnClickListener(v -> {
            ChangePasswordDialog dialog = ChangePasswordDialog.newInstance();
            dialog.setOnPasswordChangedListener(() -> {
                Toast.makeText(getContext(), "Пароль изменен. Войдите заново.", Toast.LENGTH_LONG).show();
                logout();
            });
            dialog.show(getChildFragmentManager(), "change_password");
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadProfile() {
        viewModel.loadProfile();
    }

    private void handleProfileResult(Resource<ProfileResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ProfileResponse data = ((Resource.Success<ProfileResponse>) resource).getData();
            if (data != null) {
                displayProfile(data);
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), ((Resource.Error<ProfileResponse>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayProfile(ProfileResponse data) {
        ProfileResponse.UserInfo user = data.getUser();
        ProfileResponse.ClientInfo client = data.getClient();

        String fullName = (client.getFirstName() != null ? client.getFirstName() : "") + " " +
                (client.getLastName() != null ? client.getLastName() : "");
        tvName.setText(fullName.trim().isEmpty() ? "Не указано" : fullName.trim());
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Не указан");
        tvBirthDate.setText(client.getBirthDate() != null ? client.getBirthDate() : "Не указана");

        String roleText = "ADMIN".equals(user.getRole()) ? "👑 Мастер" : "👤 Клиент";
        tvRole.setText(roleText);

        tvRegisteredAt.setText(user.getCreatedAt() != null ? user.getCreatedAt() : "Не указана");
    }

    private void handleUpdateResult(Resource<Void> resource) {
        if (resource instanceof Resource.Loading) {
            // прогресс уже показан в диалоге
        } else if (resource instanceof Resource.Success) {
            Toast.makeText(getContext(), "✅ Профиль обновлен", Toast.LENGTH_SHORT).show();
            loadProfile();
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), "❌ " + ((Resource.Error<Void>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePasswordResult(Resource<Void> resource) {
        if (resource instanceof Resource.Loading) {
            // прогресс уже показан в диалоге
        } else if (resource instanceof Resource.Success) {
            Toast.makeText(getContext(), "✅ Пароль изменен", Toast.LENGTH_SHORT).show();
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), "❌ " + ((Resource.Error<Void>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Выход из системы")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> logout())
                .setNegativeButton("Нет", null)
                .show();
    }

    private void logout() {
        if (tokenManager != null) {
            tokenManager.clear();
        }
        Toast.makeText(getContext(), "Вы вышли из системы", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}