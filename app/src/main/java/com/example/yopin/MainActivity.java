package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Инициализация Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            
            // Добавляем небольшую задержку перед переходом
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Перенаправление на экран входа
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 500); // 500 миллисекунд задержки
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка при запуске приложения: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}