package com.example.frolovnails.admin;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.ClientsAdapter;
import com.example.frolovnails.common.RefreshableFragment;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.ClientListItem;
import com.example.frolovnails.network.models.response.ClientsListResponse;
import com.example.frolovnails.ui.ClientDetailsDialog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ClientsFragment extends RefreshableFragment {

    private RecyclerView rvClients;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ClientsAdapter adapter;
    private ClientViewModel viewModel;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_clients;
    }

    @Override
    protected int getSwipeRefreshId() {
        return R.id.swipeRefreshClients;
    }

    @Override
    protected void initViews(View view) {
        rvClients = view.findViewById(R.id.rvClients);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvClients.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ClientsAdapter();
        rvClients.setAdapter(adapter);

        adapter.setOnClientClickListener(client -> {
            ClientDetailsDialog dialog = ClientDetailsDialog.newInstance(client.getId());
            dialog.show(getChildFragmentManager(), "client_details");
        });

        TokenManager tokenManager = null;
        try {
            tokenManager = new TokenManager(requireContext());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        final TokenManager finalTokenManager = tokenManager;

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ClientViewModel(finalTokenManager);
            }
        }).get(ClientViewModel.class);
    }

    @Override
    protected void loadData() {
        viewModel.loadClients(0, 20, null);
        viewModel.getClientsResult().observe(getViewLifecycleOwner(), this::handleClientsResult);
    }

    @Override
    protected void onRefresh() {
        loadData();
    }

    private void handleClientsResult(Resource<ClientsListResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            rvClients.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
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
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            rvClients.setVisibility(View.GONE);
            tvEmpty.setText("Ошибка: " + ((Resource.Error<ClientsListResponse>) resource).getMessage());
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        }
    }
}