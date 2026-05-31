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

public class TopClientsAdapter extends RecyclerView.Adapter<TopClientsAdapter.ClientViewHolder> {

    private List<DashboardStatsResponse.TopClientStats> clients = new ArrayList<>();

    public void setClients(List<DashboardStatsResponse.TopClientStats> clients) {
        this.clients = clients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        DashboardStatsResponse.TopClientStats client = clients.get(position);
        holder.bind(client, position + 1);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvClientName, tvPhone, tvVisits, tvSpent, tvFavoriteService;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvVisits = itemView.findViewById(R.id.tvVisits);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvFavoriteService = itemView.findViewById(R.id.tvFavoriteService);
        }

        void bind(DashboardStatsResponse.TopClientStats client, int rank) {
            tvRank.setText(String.valueOf(rank));
            String fullName = (client.getFirstName() != null ? client.getFirstName() : "") + " " +
                    (client.getLastName() != null ? client.getLastName() : "");
            tvClientName.setText(fullName.trim().isEmpty() ? "Без имени" : fullName.trim());
            tvPhone.setText(client.getPhone() != null ? client.getPhone() : "");
            tvVisits.setText(client.getTotalVisits() + " визитов");
            tvSpent.setText(client.getTotalSpent() + " ₽");
            tvFavoriteService.setText(client.getFavoriteService() != null ? client.getFavoriteService() : "");
        }
    }
}