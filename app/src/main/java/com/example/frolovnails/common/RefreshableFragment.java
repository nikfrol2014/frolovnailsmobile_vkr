package com.example.frolovnails.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class RefreshableFragment extends Fragment {

    protected SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        swipeRefresh = view.findViewById(getSwipeRefreshId());

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                onRefresh();
                swipeRefresh.setRefreshing(false);
            });
            swipeRefresh.setColorSchemeColors(
                    getResources().getColor(android.R.color.holo_blue_dark),
                    getResources().getColor(android.R.color.holo_green_dark),
                    getResources().getColor(android.R.color.holo_orange_dark)
            );
        }

        initViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    protected abstract int getLayoutResId();
    protected abstract int getSwipeRefreshId();
    protected abstract void initViews(View view);
    protected abstract void loadData();
    protected abstract void onRefresh();
}