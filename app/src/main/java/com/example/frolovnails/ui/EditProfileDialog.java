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
import com.example.frolovnails.network.models.request.UpdateProfileRequest;
import com.example.frolovnails.network.models.response.ProfileResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EditProfileDialog extends DialogFragment {

    private static final String ARG_FIRST_NAME = "first_name";
    private static final String ARG_LAST_NAME = "last_name";
    private static final String ARG_BIRTH_DATE = "birth_date";

    private EditText etFirstName, etLastName, etBirthDate;
    private Button btnSave, btnCancel;
    private View progressBar;
    private ProfileViewModel viewModel;
    private OnSaveListener onSaveListener;

    private String originalFirstName;
    private String originalLastName;
    private String originalBirthDate;

    public interface OnSaveListener {
        void onSave();
    }

    public static EditProfileDialog newInstance(ProfileResponse profile) {
        EditProfileDialog fragment = new EditProfileDialog();
        Bundle args = new Bundle();
        if (profile != null && profile.getClient() != null) {
            args.putString(ARG_FIRST_NAME, profile.getClient().getFirstName());
            args.putString(ARG_LAST_NAME, profile.getClient().getLastName());
            args.putString(ARG_BIRTH_DATE, profile.getClient().getBirthDate());
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.onSaveListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Редактирование профиля");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etBirthDate = view.findViewById(R.id.etBirthDate);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        if (getArguments() != null) {
            originalFirstName = getArguments().getString(ARG_FIRST_NAME);
            originalLastName = getArguments().getString(ARG_LAST_NAME);
            originalBirthDate = getArguments().getString(ARG_BIRTH_DATE);

            etFirstName.setText(originalFirstName != null ? originalFirstName : "");
            etLastName.setText(originalLastName != null ? originalLastName : "");
            etBirthDate.setText(originalBirthDate != null ? originalBirthDate : "");
        }

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

        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        // Проверяем, изменилось ли что-то
        boolean firstNameChanged = !firstName.equals(originalFirstName != null ? originalFirstName : "");
        boolean lastNameChanged = !lastName.equals(originalLastName != null ? originalLastName : "");
        boolean birthDateChanged = !birthDate.equals(originalBirthDate != null ? originalBirthDate : "");

        if (!firstNameChanged && !lastNameChanged && !birthDateChanged) {
            Toast.makeText(getContext(), "Ничего не изменено", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName(firstName.isEmpty() ? null : firstName);
        request.setLastName(lastName.isEmpty() ? null : lastName);
        request.setBirthDate(birthDate.isEmpty() ? null : birthDate);
        request.setNotes(null);

        viewModel.updateProfile(request);
        viewModel.getUpdateProfileResult().observe(getViewLifecycleOwner(), this::handleResult);
    }

    private void handleResult(Resource<Void> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "✅ Профиль обновлен", Toast.LENGTH_SHORT).show();
            if (onSaveListener != null) {
                onSaveListener.onSave();
            }
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            Toast.makeText(getContext(), ((Resource.Error<Void>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}