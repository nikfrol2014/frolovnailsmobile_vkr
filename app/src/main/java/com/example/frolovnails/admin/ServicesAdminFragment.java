package com.example.frolovnails.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.ServicesAdminAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.request.ServiceRequest;
import com.example.frolovnails.network.models.response.Service;
import com.example.frolovnails.ui.ServiceDialog;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class ServicesAdminFragment extends Fragment {

    private RecyclerView rvServices;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MaterialButton btnAddService;
    private ServicesAdminAdapter adapter;
    private ServicesAdminViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_services_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvServices = view.findViewById(R.id.rvServicesAdmin);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnAddService = view.findViewById(R.id.btnAddService);

        rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ServicesAdminAdapter();
        rvServices.setAdapter(adapter);

        adapter.setOnServiceActionListener(new ServicesAdminAdapter.OnServiceActionListener() {
            @Override
            public void onEditClick(Service service) {
                ServiceDialog dialog = ServiceDialog.newInstance(service);
                if (dialog != null) {
                    dialog.show(getChildFragmentManager(), "edit_service");
                }
            }

            @Override
            public void onToggleActiveClick(Service service) {
                if (service.getActive() != null && service.getActive()) {
                    // Деактивируем — DELETE
                    viewModel.deactivateService(service.getId());
                } else {
                    // Активируем — PATCH
                    viewModel.activateService(service.getId());
                }
            }
        });

        btnAddService.setOnClickListener(v -> {
            ServiceDialog dialog = ServiceDialog.newInstance();
            if (dialog != null) {
                dialog.show(getChildFragmentManager(), "add_service");
            }
        });

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

        // Наблюдатели
        viewModel.getServicesResult().observe(getViewLifecycleOwner(), this::handleServicesResult);
        viewModel.getCreateResult().observe(getViewLifecycleOwner(), this::handleCreateResult);
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), this::handleUpdateResult);
        viewModel.getActivateResult().observe(getViewLifecycleOwner(), this::handleActivateResult);
        viewModel.getDeactivateResult().observe(getViewLifecycleOwner(), this::handleDeactivateResult);

        // Загружаем все услуги
        viewModel.loadAllServices();
    }

    private void handleServicesResult(Resource<List<Service>> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            rvServices.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            List<Service> services = ((Resource.Success<List<Service>>) resource).getData();

            if (services == null || services.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvServices.setVisibility(View.GONE);
                tvEmpty.setText("Нет услуг");
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvServices.setVisibility(View.VISIBLE);
                adapter.setServices(new ArrayList<>(services));
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            rvServices.setVisibility(View.GONE);
            tvEmpty.setText("Ошибка: " + ((Resource.Error<List<Service>>) resource).getMessage());
        }
    }

    private void handleCreateResult(Resource<Service> resource) {
        if (resource instanceof Resource.Success) {
            Toast.makeText(getContext(), "Услуга добавлена", Toast.LENGTH_SHORT).show();
            viewModel.loadAllServices();
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), ((Resource.Error<Service>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUpdateResult(Resource<Service> resource) {
        if (resource instanceof Resource.Success) {
            Toast.makeText(getContext(), "Услуга обновлена", Toast.LENGTH_SHORT).show();
            viewModel.loadAllServices();
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), ((Resource.Error<Service>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleActivateResult(Resource<Service> resource) {
        if (resource instanceof Resource.Success) {
            Toast.makeText(getContext(), "Услуга активирована", Toast.LENGTH_SHORT).show();
            viewModel.loadAllServices();
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), ((Resource.Error<Service>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDeactivateResult(Resource<Void> resource) {
        if (resource instanceof Resource.Success) {
            Toast.makeText(getContext(), "Услуга деактивирована", Toast.LENGTH_SHORT).show();
            viewModel.loadAllServices();
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), ((Resource.Error<Void>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}