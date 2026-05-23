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
import com.example.frolovnails.adapters.ScheduleBlocksAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.ScheduleBlock;
import com.example.frolovnails.ui.ScheduleBlockDialog;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ScheduleBlocksTabFragment extends Fragment {

    private RecyclerView rvBlocks;
    private ProgressBar progressBlocks;
    private TextView tvEmptyBlocks;
    private MaterialButton btnAddBlock;
    private ScheduleBlocksAdapter adapter;
    private ScheduleViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_blocks_tab, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBlocks(); // Обновляем список при возвращении на фрагмент
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvBlocks = view.findViewById(R.id.rvBlocks);
        progressBlocks = view.findViewById(R.id.progressBlocks);
        tvEmptyBlocks = view.findViewById(R.id.tvEmptyBlocks);
        btnAddBlock = view.findViewById(R.id.btnAddBlock);

        rvBlocks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleBlocksAdapter();
        rvBlocks.setAdapter(adapter);

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
                return (T) new ScheduleViewModel(finalTokenManager);
            }
        }).get(ScheduleViewModel.class);

        adapter.setOnItemClickListener(item -> {
            viewModel.deleteScheduleBlock(item.getId());
            viewModel.getDeleteScheduleBlockResult().observe(getViewLifecycleOwner(), resource -> {
                if (resource instanceof Resource.Success) {
                    loadBlocks();
                }
            });
        });

        btnAddBlock.setOnClickListener(v -> {
            ScheduleBlockDialog dialog = ScheduleBlockDialog.newInstance();
            dialog.show(getChildFragmentManager(), "add_block");
        });

        loadBlocks();
    }

    private void loadBlocks() {
        viewModel.loadScheduleBlocks(3);
        viewModel.getScheduleBlocksResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) {
                progressBlocks.setVisibility(View.VISIBLE);
                rvBlocks.setVisibility(View.GONE);
                tvEmptyBlocks.setVisibility(View.GONE);
            } else if (resource instanceof Resource.Success) {
                progressBlocks.setVisibility(View.GONE);
                List<ScheduleBlock> data = ((Resource.Success<List<ScheduleBlock>>) resource).getData();
                if (data == null || data.isEmpty()) {
                    tvEmptyBlocks.setVisibility(View.VISIBLE);
                    rvBlocks.setVisibility(View.GONE);
                } else {
                    tvEmptyBlocks.setVisibility(View.GONE);
                    rvBlocks.setVisibility(View.VISIBLE);
                    adapter.setItems(data);
                }
            } else if (resource instanceof Resource.Error) {
                progressBlocks.setVisibility(View.GONE);
                tvEmptyBlocks.setVisibility(View.VISIBLE);
                tvEmptyBlocks.setText("Ошибка: " + ((Resource.Error<List<ScheduleBlock>>) resource).getMessage());
            }
        });
    }
}