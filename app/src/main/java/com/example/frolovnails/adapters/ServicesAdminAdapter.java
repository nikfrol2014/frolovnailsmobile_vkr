package com.example.frolovnails.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.Service;

import java.util.ArrayList;
import java.util.List;

public class ServicesAdminAdapter extends RecyclerView.Adapter<ServicesAdminAdapter.ServiceViewHolder> {

    private List<Service> services = new ArrayList<>();
    private OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onEditClick(Service service);
        void onToggleActiveClick(Service service);
    }

    public void setOnServiceActionListener(OnServiceActionListener listener) {
        this.listener = listener;
    }

    public void setServices(List<Service> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_admin, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service, listener);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServiceDescription, tvDuration, tvPrice, tvCategory;
        Button btnEdit, btnToggleActive;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnToggleActive = itemView.findViewById(R.id.btnToggleActive);
        }

        void bind(Service service, OnServiceActionListener listener) {
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription() != null ? service.getDescription() : "");
            tvDuration.setText(service.getDurationMinutes() + " мин");

            // Форматируем цену с приставкой "от"
            String priceText = (service.getPrice() != null && service.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0)
                    ? "от " + service.getPrice() + " ₽"
                    : "Цена не указана";
            tvPrice.setText(priceText);

            tvCategory.setText(service.getCategory());

            // Меняем текст кнопки в зависимости от статуса
            if (service.getActive() != null && service.getActive()) {
                btnToggleActive.setText("Деактивировать");
                btnToggleActive.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            } else {
                btnToggleActive.setText("Активировать");
                btnToggleActive.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(service);
                }
            });

            btnToggleActive.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                        .setTitle(service.getActive() ? "Деактивация услуги" : "Активация услуги")
                        .setMessage(service.getActive()
                                ? "Вы уверены, что хотите деактивировать услугу \"" + service.getName() + "\"?\nОна пропадёт из списка доступных для клиентов."
                                : "Вы уверены, что хотите активировать услугу \"" + service.getName() + "\"?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            if (listener != null) {
                                listener.onToggleActiveClick(service);
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .show();
            });
        }
    }
}