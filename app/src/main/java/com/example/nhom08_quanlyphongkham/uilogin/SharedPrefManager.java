package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import com.example.nhom08_quanlyphongkham.UserProfile;
import com.google.gson.Gson;

public class SharedPrefManager {
    private static final String PREF_NAME = "name";
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_REFRESH_TOKEN = "key_refresh_token";
    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;

    // Constructor
    private SharedPrefManager(Context context) {
        try{
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        }
        catch (Exception e)
        {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            android.util.Log.d("SharedPrefManager", "Default SharedPreferences.");
        }
    }

    // Khởi tạo instance (Singleton pattern)
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // 1. Hàm lưu Token
    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    // 2. Hàm lấy Token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    // 3. Hàm xóa dữ liệu (Dùng khi Logout)
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    public void saveProfile(UserProfile profile) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(profile);

        editor.putString("user_profile", json);
        editor.apply();
    }

    public UserProfile getProfile() {
        String json = sharedPreferences.getString("user_profile", null);

        if (json == null) return null;

        Gson gson = new Gson();
        return gson.fromJson(json, UserProfile.class); // Chuyển JSON ngược thành Object
    }
}