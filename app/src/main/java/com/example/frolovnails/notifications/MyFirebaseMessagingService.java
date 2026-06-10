package com.example.frolovnails.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.frolovnails.MainActivity;
import com.example.frolovnails.R;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.ApiClient;
import com.example.frolovnails.network.ApiService;
import com.example.frolovnails.network.models.response.ApiResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    private static final String CHANNEL_ID = "frolovnails_channel";
    private static final String CHANNEL_NAME = "Frolov's Nails";
    private static final String CHANNEL_DESC = "Уведомления о записях";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New FCM token: " + token);
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Map<String, String> data = remoteMessage.getData();

            Log.d(TAG, "Notification: title=" + title + ", body=" + body);
            showNotification(title, body, data);
        }
    }

    private void sendTokenToServer(String token) {
        try {
            TokenManager tokenManager = new TokenManager(this);
            String accessToken = tokenManager.getAccessToken();

            if (accessToken != null) {
                ApiService apiService = ApiClient.getClient(tokenManager).create(ApiService.class);

                Map<String, String> request = new HashMap<>();
                request.put("token", token);

                apiService.saveFcmToken(request).enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Token saved on server");
                        } else {
                            Log.e(TAG, "Failed to save token: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "Error saving token: " + t.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending token to server: " + e.getMessage());
        }
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (data.containsKey("appointmentId")) {
            try {
                intent.putExtra("appointment_id", Long.parseLong(data.get("appointmentId")));
                intent.putExtra("notification_type", data.get("type"));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing appointmentId", e);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}