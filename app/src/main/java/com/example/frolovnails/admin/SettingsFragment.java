package com.example.frolovnails.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.frolovnails.MainActivity;
import com.example.frolovnails.R;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.ProfileResponse;
import com.example.frolovnails.ui.ChangePasswordDialog;
import com.example.frolovnails.ui.EditProfileDialog;
import com.example.frolovnails.utils.ToastUtils;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SettingsFragment extends Fragment {

    private MaterialCardView cardSchedule, cardProfile, cardChangePassword, cardLogout;
    private ProfileViewModel profileViewModel;
    private TokenManager tokenManager;
    private View progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
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
        cardSchedule = view.findViewById(R.id.cardSchedule);
        cardProfile = view.findViewById(R.id.cardProfile);
        cardChangePassword = view.findViewById(R.id.cardChangePassword);
        cardLogout = view.findViewById(R.id.cardLogout);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void initViewModel() {
        try {
            tokenManager = new TokenManager(requireContext());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        profileViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ProfileViewModel(tokenManager);
            }
        }).get(ProfileViewModel.class);
    }

    private void loadProfile() {
        profileViewModel.loadProfile();
        profileViewModel.getProfileResult().observe(getViewLifecycleOwner(), this::handleProfileResult);
    }

    private void handleProfileResult(Resource<ProfileResponse> resource) {
        if (resource instanceof Resource.Success) {
            // Профиль загружен, сохраняем в ViewModel
        } else if (resource instanceof Resource.Error) {
            // Ошибка загрузки, но не показываем чтобы не раздражать
        }
    }

    private void setupClickListeners() {
        // Расписание - обычный переход
        cardSchedule.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.nav_schedule);
        });

        // Редактировать профиль
        cardProfile.setOnClickListener(v -> {
            if (profileViewModel.getProfileData() != null) {
                EditProfileDialog dialog = EditProfileDialog.newInstance(profileViewModel.getProfileData());
                dialog.setOnSaveListener(() -> {
                    profileViewModel.loadProfile();
                    ToastUtils.show(getContext(), "Профиль обновлен", Toast.LENGTH_SHORT);
                });
                dialog.show(getChildFragmentManager(), "edit_profile");
            } else {
                ToastUtils.show(getContext(), "Загрузка данных профиля...", Toast.LENGTH_SHORT);
            }
        });

        // Сменить пароль
        cardChangePassword.setOnClickListener(v -> {
            ChangePasswordDialog dialog = ChangePasswordDialog.newInstance();
            dialog.setOnPasswordChangedListener(() -> {
                ToastUtils.show(getContext(), "Пароль изменен. Войдите заново.", Toast.LENGTH_LONG);
                logout();
            });
            dialog.show(getChildFragmentManager(), "change_password");
        });

        // Выход
        cardLogout.setOnClickListener(v -> showLogoutDialog());
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
        ToastUtils.show(getContext(), "Вы вышли из системы", Toast.LENGTH_SHORT);

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}