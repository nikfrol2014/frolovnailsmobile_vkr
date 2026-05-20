package com.example.frolovnails.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.ServicesAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ServicesFragment extends Fragment {

    private RecyclerView rvServices;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ServicesViewModel viewModel;
    private ServicesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvServices = view.findViewById(R.id.rvServices);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ServicesAdapter();
        rvServices.setAdapter(adapter);

        // Инициализация TokenManager
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
                return (T) new ServicesViewModel(finalTokenManager);
            }
        }).get(ServicesViewModel.class);

        viewModel.loadServices();
        viewModel.getServicesResult().observe(getViewLifecycleOwner(), this::handleServicesResult);
    }

    @SuppressWarnings("unchecked")
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
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvServices.setVisibility(View.VISIBLE);
                adapter.setServices(services);
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            rvServices.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Ошибка: " + ((Resource.Error<List<Service>>) resource).getMessage());
        }
    }
}