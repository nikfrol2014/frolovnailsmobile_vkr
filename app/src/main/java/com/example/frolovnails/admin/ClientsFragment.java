package com.example.frolovnails.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvClients;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EditText etSearch;
    private ImageButton btnSort;
    private ClientsAdapter adapter;
    private ClientViewModel viewModel;
    private String currentSortType = "name";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefreshClients);
        rvClients = view.findViewById(R.id.rvClients);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        etSearch = view.findViewById(R.id.etSearch);
        btnSort = view.findViewById(R.id.btnSort);

        rvClients.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ClientsAdapter();
        rvClients.setAdapter(adapter);

        adapter.setOnClientClickListener(client -> {
            ClientDetailsDialog dialog = ClientDetailsDialog.newInstance(client.getId());
            dialog.show(getChildFragmentManager(), "client_details");
        });

        // Поиск
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Сортировка
        btnSort.setOnClickListener(v -> showSortMenu());

        // SwipeRefresh
        swipeRefresh.setOnRefreshListener(() -> {
            loadClients();
            swipeRefresh.setRefreshing(false);
        });
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_dark),
                getResources().getColor(android.R.color.holo_green_dark),
                getResources().getColor(android.R.color.holo_orange_dark)
        );

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
        viewModel.loadClients(0, 100, null);
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

    private void showSortMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), btnSort);
        popupMenu.inflate(R.menu.sort_clients_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_by_name) {
                currentSortType = "name";
                viewModel.sortBy("name");
                return true;
            } else if (itemId == R.id.sort_by_visits) {
                currentSortType = "visits";
                viewModel.sortBy("visits");
                return true;
            } else if (itemId == R.id.sort_by_spent) {
                currentSortType = "spent";
                viewModel.sortBy("spent");
                return true;
            }
            return false;
        });
        popupMenu.show();
    }
}