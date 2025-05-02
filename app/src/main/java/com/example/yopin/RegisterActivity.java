package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword, etFullName, etUsername, etBirthDate;
    private Button btnRegister;
    private TextView tvLogin;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация менеджеров
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        firebaseManager = FirebaseManager.getInstance(this);

        // Инициализация UI элементов
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etBirthDate = findViewById(R.id.etBirthDate);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // По умолчанию устанавливаем текущую дату
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        etBirthDate.setText(sdf.format(new Date()));

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        // Валидация полей
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty() || username.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        // Регистрация в Firebase и локальной базе
        firebaseManager.registerUser(email, password, fullName, username, birthDate, new FirebaseManager.OnAuthCompleteListener() {
            @Override
            public void onSuccess() {
                // Получаем данные пользователя из локальной базы
                User user = dbHelper.getUser(email);
                if (user != null) {
                    // Создаем сессию
                    sessionManager.createLoginSession(user.getId(), email);
                    
                    Toast.makeText(RegisterActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, BooksActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Если Firebase регистрация не удалась, пробуем только локальную
                if (dbHelper.addUser(email, password, fullName, username, birthDate)) {
                    User user = dbHelper.getUser(email);
                    if (user != null) {
                        sessionManager.createLoginSession(user.getId(), email);
                    }
                    
                    Toast.makeText(RegisterActivity.this, "Регистрация успешна (оффлайн режим)", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, BooksActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Ошибка регистрации. Возможно, пользователь уже существует", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}