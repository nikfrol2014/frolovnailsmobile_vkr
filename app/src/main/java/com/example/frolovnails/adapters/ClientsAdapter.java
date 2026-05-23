package com.example.frolovnails.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        TextView tvAvatar, tvName, tvPhone, tvVisits, tvTotalSpent;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvVisits = itemView.findViewById(R.id.tvVisits);
            tvTotalSpent = itemView.findViewById(R.id.tvTotalSpent);
        }

        void bind(ClientListItem client, OnClientClickListener listener) {
            String firstName = client.getFirstName() != null ? client.getFirstName() : "";
            String lastName = client.getLastName() != null ? client.getLastName() : "";
            String fullName = firstName + " " + lastName;
            tvName.setText(fullName.trim().isEmpty() ? "Без имени" : fullName.trim());

            // Аватар (первая буква имени)
            String firstLetter = firstName.length() > 0 ? String.valueOf(firstName.charAt(0)).toUpperCase() : "?";
            tvAvatar.setText(firstLetter);

            tvPhone.setText(client.getPhone() != null ? client.getPhone() : "Нет телефона");

            int visits = client.getTotalVisits() != null ? client.getTotalVisits() : 0;
            tvVisits.setText(visits + " визитов");

            // Показываем заглушку для потраченной суммы (если нет данных)
            tvTotalSpent.setText("— ₽");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClientClick(client);
                }
            });
        }
    }
}