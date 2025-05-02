package com.example.yopin;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "YOpinSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    
    // Сохранить данные о входе пользователя
    public void createLoginSession(int userId, String email) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    // Получить ID пользователя
    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }
    
    // Получить Email пользователя
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }
    
    // Проверить, авторизован ли пользователь
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    // Очистить данные сессии при выходе
    public void logout() {
        editor.clear();
        editor.apply();
    }
} 