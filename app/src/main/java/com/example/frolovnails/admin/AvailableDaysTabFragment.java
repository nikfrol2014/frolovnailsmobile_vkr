package com.example.frolovnails.admin;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.AvailableDaysAdapter;
import com.example.frolovnails.common.RefreshableFragment;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.AvailableDay;
import com.example.frolovnails.ui.AvailableDayDialog;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class AvailableDaysTabFragment extends RefreshableFragment {

    private RecyclerView rvAvailableDays;
    private ProgressBar progressDays;
    private TextView tvEmptyDays;
    private MaterialButton btnAddDay;
    private AvailableDaysAdapter adapter;
    private ScheduleViewModel viewModel;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_available_days_tab;
    }

    @Override
    protected int getSwipeRefreshId() {
        return R.id.swipeRefreshDays;
    }

    @Override
    protected void initViews(View view) {
        rvAvailableDays = view.findViewById(R.id.rvAvailableDays);
        progressDays = view.findViewById(R.id.progressDays);
        tvEmptyDays = view.findViewById(R.id.tvEmptyDays);
        btnAddDay = view.findViewById(R.id.btnAddDay);

        rvAvailableDays.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AvailableDaysAdapter();
        rvAvailableDays.setAdapter(adapter);

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
                return (T) new ScheduleViewModel(finalTokenManager);
            }
        }).get(ScheduleViewModel.class);

        adapter.setOnItemClickListener(new AvailableDaysAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(AvailableDay item) {
                AvailableDayDialog dialog = AvailableDayDialog.newInstance(item);
                dialog.show(getChildFragmentManager(), "edit_day");
            }

            @Override
            public void onDeleteClick(AvailableDay item) {
                viewModel.deleteAvailableDay(item.getId());
                viewModel.getDeleteAvailableDayResult().observe(getViewLifecycleOwner(), resource -> {
                    if (resource instanceof Resource.Success) {
                        loadData();
                    }
                });
            }
        });

        btnAddDay.setOnClickListener(v -> {
            AvailableDayDialog dialog = AvailableDayDialog.newInstance();
            dialog.show(getChildFragmentManager(), "add_day");
        });
    }

    @Override
    protected void loadData() {
        viewModel.loadAvailableDays(3);
        viewModel.getAvailableDaysResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) {
                progressDays.setVisibility(View.VISIBLE);
                rvAvailableDays.setVisibility(View.GONE);
                tvEmptyDays.setVisibility(View.GONE);
            } else if (resource instanceof Resource.Success) {
                progressDays.setVisibility(View.GONE);
                List<AvailableDay> data = ((Resource.Success<List<AvailableDay>>) resource).getData();
                if (data == null || data.isEmpty()) {
                    tvEmptyDays.setVisibility(View.VISIBLE);
                    rvAvailableDays.setVisibility(View.GONE);
                } else {
                    tvEmptyDays.setVisibility(View.GONE);
                    rvAvailableDays.setVisibility(View.VISIBLE);
                    adapter.setItems(data);
                }
            } else if (resource instanceof Resource.Error) {
                progressDays.setVisibility(View.GONE);
                tvEmptyDays.setVisibility(View.VISIBLE);
                tvEmptyDays.setText("Ошибка: " + ((Resource.Error<List<AvailableDay>>) resource).getMessage());
            }
        });
    }

    @Override
    protected void onRefresh() {
        loadData();
    }
}