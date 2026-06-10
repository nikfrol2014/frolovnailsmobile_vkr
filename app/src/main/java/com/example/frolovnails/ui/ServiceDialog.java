package com.example.frolovnails.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.frolovnails.admin.ServicesAdminViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.request.ServiceRequest;
import com.example.frolovnails.network.models.response.Service;
import com.example.frolovnails.utils.ToastUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;

public class ServiceDialog extends DialogFragment {

    private static final String ARG_SERVICE_ID = "service_id";
    private static final String ARG_SERVICE_NAME = "service_name";
    private static final String ARG_SERVICE_DESCRIPTION = "service_description";
    private static final String ARG_SERVICE_DURATION = "service_duration";
    private static final String ARG_SERVICE_PRICE = "service_price";
    private static final String ARG_SERVICE_CATEGORY = "service_category";

    private EditText etName, etDescription, etDuration, etPrice, etCategory;
    private Button btnSave, btnCancel;
    private View progressBar;
    private ServicesAdminViewModel viewModel;

    private Long editingServiceId = null;
    private String editingName;
    private String editingDescription;
    private int editingDuration;
    private BigDecimal editingPrice;
    private String editingCategory;

    public static ServiceDialog newInstance() {
        return new ServiceDialog();
    }

    public static ServiceDialog newInstance(Service service) {
        ServiceDialog fragment = new ServiceDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_SERVICE_ID, service.getId());
        args.putString(ARG_SERVICE_NAME, service.getName());
        args.putString(ARG_SERVICE_DESCRIPTION, service.getDescription());
        args.putInt(ARG_SERVICE_DURATION, service.getDurationMinutes());
        args.putSerializable(ARG_SERVICE_PRICE, service.getPrice());
        args.putString(ARG_SERVICE_CATEGORY, service.getCategory());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_SERVICE_ID)) {
            editingServiceId = getArguments().getLong(ARG_SERVICE_ID);
            editingName = getArguments().getString(ARG_SERVICE_NAME);
            editingDescription = getArguments().getString(ARG_SERVICE_DESCRIPTION);
            editingDuration = getArguments().getInt(ARG_SERVICE_DURATION);
            editingPrice = (BigDecimal) getArguments().getSerializable(ARG_SERVICE_PRICE);
            editingCategory = getArguments().getString(ARG_SERVICE_CATEGORY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (editingServiceId != null) {
            dialog.setTitle("Редактирование услуги");
        } else {
            dialog.setTitle("Добавление услуги");
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_service, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        etDuration = view.findViewById(R.id.etDuration);
        etPrice = view.findViewById(R.id.etPrice);
        etCategory = view.findViewById(R.id.etCategory);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        // Настройка ввода
        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etDescription.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etCategory.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // Заполняем поля при редактировании
        if (editingServiceId != null) {
            etName.setText(editingName != null ? editingName : "");
            etDescription.setText(editingDescription != null ? editingDescription : "");
            etDuration.setText(String.valueOf(editingDuration));
            etPrice.setText(editingPrice != null ? editingPrice.toString() : "");
            etCategory.setText(editingCategory != null ? editingCategory : "");
        }

        // Инициализация ViewModel
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
                return (T) new ServicesAdminViewModel(finalTokenManager);
            }
        }).get(ServicesAdminViewModel.class);

        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveService() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (name.isEmpty()) {
            ToastUtils.show(getContext(), "Введите название услуги", Toast.LENGTH_SHORT);
            return;
        }
        if (durationStr.isEmpty()) {
            ToastUtils.show(getContext(), "Введите длительность", Toast.LENGTH_SHORT);
            return;
        }
        if (priceStr.isEmpty()) {
            ToastUtils.show(getContext(), "Введите цену", Toast.LENGTH_SHORT);
            return;
        }
        if (category.isEmpty()) {
            ToastUtils.show(getContext(), "Введите категорию", Toast.LENGTH_SHORT);
            return;
        }

        int duration;
        BigDecimal price;
        try {
            duration = Integer.parseInt(durationStr);
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            ToastUtils.show(getContext(), "Неверный формат числа", Toast.LENGTH_SHORT);
            return;
        }

        ServiceRequest request = new ServiceRequest();
        request.setName(name);
        request.setDescription(description);
        request.setDurationMinutes(duration);
        request.setPrice(price);
        request.setCategory(category);

        if (editingServiceId != null) {
            viewModel.updateService(editingServiceId, request);
            viewModel.getUpdateResult().observe(getViewLifecycleOwner(), this::handleUpdateResult);
        } else {
            viewModel.createService(request);
            viewModel.getCreateResult().observe(getViewLifecycleOwner(), this::handleCreateResult);
        }
    }

    private void handleCreateResult(Resource<Service> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "Услуга добавлена", Toast.LENGTH_SHORT);
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<Service>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }

    private void handleUpdateResult(Resource<Service> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "Услуга обновлена", Toast.LENGTH_SHORT);
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<Service>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }
}