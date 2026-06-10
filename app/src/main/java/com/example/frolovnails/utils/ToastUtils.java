package com.example.frolovnails.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frolovnails.R;

public class ToastUtils {

    private static Toast toast;

    public static void show(Context context, String message) {
        show(context, message, R.drawable.ic_logo);
    }

    public static void show(Context context, String message, int iconRes) {
        if (toast != null) {
            toast.cancel();
        }

        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.custom_toast, null);

            ImageView icon = layout.findViewById(R.id.toast_icon);
            TextView text = layout.findViewById(R.id.toast_text);

            // Если передан 0 или некорректный ресурс, используем иконку по умолчанию
            if (iconRes == 0) {
                iconRes = R.drawable.ic_logo;
            }

            icon.setImageResource(iconRes);
            text.setText(message);

            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();

        } catch (Exception e) {
            // Fallback на обычный Toast
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    // Для длинных сообщений
    public static void showLong(Context context, String message) {
        showLong(context, message, R.drawable.ic_logo);
    }

    public static void showLong(Context context, String message, int iconRes) {
        if (toast != null) {
            toast.cancel();
        }

        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.custom_toast, null);

            ImageView icon = layout.findViewById(R.id.toast_icon);
            TextView text = layout.findViewById(R.id.toast_text);

            if (iconRes == 0) {
                iconRes = R.drawable.ic_logo;
            }

            icon.setImageResource(iconRes);
            text.setText(message);

            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

        } catch (Exception e) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}