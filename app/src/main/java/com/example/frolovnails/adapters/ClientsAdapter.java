package com.example.frolovnails.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.ClientListItem;

import java.util.ArrayList;
import java.util.List;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ClientViewHolder> {

    private List<ClientListItem> clients = new ArrayList<>();
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onClientClick(ClientListItem client);
    }

    public void setOnClientClickListener(OnClientClickListener listener) {
        this.listener = listener;
    }

    public void setClients(List<ClientListItem> clients) {
        this.clients = clients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        ClientListItem client = clients.get(position);
        holder.bind(client, listener);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvVisits;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvVisits = itemView.findViewById(R.id.tvVisits);
        }

        void bind(ClientListItem client, OnClientClickListener listener) {
            String fullName = (client.getFirstName() != null ? client.getFirstName() : "") + " " +
                    (client.getLastName() != null ? client.getLastName() : "");
            tvName.setText(fullName.trim().isEmpty() ? "Без имени" : fullName.trim());
            tvPhone.setText(client.getPhone() != null ? client.getPhone() : "Нет телефона");
            tvVisits.setText("Визитов: " + (client.getTotalVisits() != null ? client.getTotalVisits() : 0));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClientClick(client);
                }
            });
        }
    }
}