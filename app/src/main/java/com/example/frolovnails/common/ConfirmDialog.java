package com.example.frolovnails.common;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class ConfirmDialog {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public static void show(Context context, String title, String message, OnConfirmListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Да", (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    public static void showDelete(Context context, String itemName, OnConfirmListener listener) {
        show(context, "Удаление", "Вы уверены, что хотите удалить \"" + itemName + "\"?", listener);
    }
}