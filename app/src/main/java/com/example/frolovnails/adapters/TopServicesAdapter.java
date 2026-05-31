package com.example.frolovnails.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.stats.DashboardStatsResponse;

import java.util.ArrayList;
import java.util.List;

public class TopServicesAdapter extends RecyclerView.Adapter<TopServicesAdapter.ServiceViewHolder> {

    private List<DashboardStatsResponse.TopServiceStats> services = new ArrayList<>();

    public void setServices(List<DashboardStatsResponse.TopServiceStats> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        DashboardStatsResponse.TopServiceStats service = services.get(position);
        holder.bind(service, position + 1);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvServiceName, tvCategory, tvCount, tvRevenue;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
        }

        void bind(DashboardStatsResponse.TopServiceStats service, int rank) {
            tvRank.setText(String.valueOf(rank));
            tvServiceName.setText(service.getServiceName());
            tvCategory.setText(service.getCategory());
            tvCount.setText(service.getCount() + " раз");
            tvRevenue.setText(service.getTotalRevenue() + " ₽");
        }
    }
}