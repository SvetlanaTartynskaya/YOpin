package com.example.yopin;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "YopinSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private static SessionManager instance;

    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    // Сохранить данные сессии
    public void createLoginSession(String email, int userId) {
        editor.putString(KEY_EMAIL, email);
        editor.putInt(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Проверить, вошел ли пользователь
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Получить данные пользователя
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getUsername() {
        return "";
    }

    // Выход из сессии
    public void logout() {
        editor.clear();
        editor.apply();
    }
} 