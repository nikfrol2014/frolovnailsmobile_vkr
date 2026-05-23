package com.example.frolovnails.admin;

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
import com.example.frolovnails.adapters.ClientsAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.ClientListItem;
import com.example.frolovnails.network.models.response.ClientsListResponse;
import com.example.frolovnails.ui.ClientDetailsDialog;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ClientsFragment extends Fragment {

    private RecyclerView rvClients;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ClientsAdapter adapter;
    private ClientViewModel viewModel;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvClients = view.findViewById(R.id.rvClients);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvClients.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ClientsAdapter();
        rvClients.setAdapter(adapter);

        adapter.setOnClientClickListener(client -> {
            // TODO: открыть детали клиента
            showClientDetailsDialog(client.getId());
        });

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
                return (T) new ClientViewModel(finalTokenManager);
            }
        }).get(ClientViewModel.class);

        loadClients();
    }

    private void loadClients() {
        viewModel.loadClients(currentPage, PAGE_SIZE, null);
        viewModel.getClientsResult().observe(getViewLifecycleOwner(), this::handleClientsResult);
    }

    private void handleClientsResult(Resource<ClientsListResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            rvClients.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ClientsListResponse data = ((Resource.Success<ClientsListResponse>) resource).getData();

            if (data == null || data.getClients() == null || data.getClients().isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvClients.setVisibility(View.GONE);
                tvEmpty.setText("Нет клиентов");
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvClients.setVisibility(View.VISIBLE);
                adapter.setClients(data.getClients());
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            rvClients.setVisibility(View.GONE);
            tvEmpty.setText("Ошибка: " + ((Resource.Error<ClientsListResponse>) resource).getMessage());
        }
    }

    private void showClientDetailsDialog(Long clientId) {
        ClientDetailsDialog dialog = ClientDetailsDialog.newInstance(clientId);
        dialog.show(getChildFragmentManager(), "client_details");
    }
}