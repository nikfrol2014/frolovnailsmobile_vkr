package com.example.frolovnails.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ROLE = "user_role";

    private final SharedPreferences sharedPreferences;
    private OnTokenRefreshListener tokenRefreshListener;

    public interface OnTokenRefreshListener {
        void onTokenRefreshed(String newAccessToken);
        void onTokenRefreshFailed();
    }

    public void setOnTokenRefreshListener(OnTokenRefreshListener listener) {
        this.tokenRefreshListener = listener;
    }

    public TokenManager(Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyGenParameterSpec(
                        new KeyGenParameterSpec.Builder(
                                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                .setKeySize(256)
                                .build())
                .build();

        sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public void saveTokens(String accessToken, String refreshToken, String role) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void updateAccessToken(String newAccessToken) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, newAccessToken)
                .apply();
        if (tokenRefreshListener != null) {
            tokenRefreshListener.onTokenRefreshed(newAccessToken);
        }
    }
}