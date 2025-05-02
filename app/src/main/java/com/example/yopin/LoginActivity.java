package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            
            // Инициализация UI элементов
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            tvRegister = findViewById(R.id.tvRegister);
            progressBar = findViewById(R.id.progressBar);
            
            // Инициализация менеджеров
            try {
                sessionManager = new SessionManager(this);
                firebaseManager = FirebaseManager.getInstance(this);
                dbHelper = new DatabaseHelper(this);
                
                // Проверка, вошел ли пользователь ранее
                if (sessionManager.isLoggedIn()) {
                    // Пользователь уже вошел, переходим к списку книг
                    Intent intent = new Intent(LoginActivity.this, BooksActivity.class);
                    intent.putExtra("email", sessionManager.getUserEmail());
                    startActivity(intent);
                    finish();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка инициализации менеджеров: " + e.getMessage(), e);
                Toast.makeText(this, "Ошибка инициализации приложения", Toast.LENGTH_LONG).show();
            }
            
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });
            
            tvRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Критическая ошибка в onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Критическая ошибка при запуске: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void login() {
        try {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }
            
            progressBar.setVisibility(View.VISIBLE);
            
            // Сначала пытаемся войти через Firebase для синхронизации
            firebaseManager.loginUser(email, password, new FirebaseManager.OnAuthCompleteListener() {
                @Override
                public void onSuccess() {
                    // Успешный вход в Firebase, проверяем локальный вход
                    if (!dbHelper.checkUser(email, password)) {
                        // Пользователь есть в Firebase, но нет в локальной базе
                        // Синхронизируем данные
                        firebaseManager.syncDataFromServer();
                    }
                    
                    // Получаем данные пользователя
                    User user = dbHelper.getUser(email);
                    if (user != null) {
                        // Создаем сессию
                        sessionManager.createLoginSession(user.getId(), email);
                        
                        Toast.makeText(LoginActivity.this, "Вход успешен", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, BooksActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    // Firebase вход не удался, пробуем войти локально
                    checkLocalLogin(email, password);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при попытке входа: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
    
    private void checkLocalLogin(String email, String password) {
        try {
            boolean isValid = dbHelper.checkUser(email, password);
            progressBar.setVisibility(View.GONE);
            
            if (isValid) {
                // Получаем данные пользователя
                User user = dbHelper.getUser(email);
                if (user != null) {
                    // Создаем сессию
                    sessionManager.createLoginSession(user.getId(), email);
                    
                    Toast.makeText(LoginActivity.this, "Вход успешен (оффлайн режим)", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, BooksActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при локальном входе: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка при локальном входе: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}