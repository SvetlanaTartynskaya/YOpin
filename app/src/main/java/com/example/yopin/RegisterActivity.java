package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Экран регистрации в приложении
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword, etFullName;
    private Button btnRegister;
    private TextView tvLogin;
    private DataRepository dataRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация объектов
        dataRepository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);
        
        // Инициализация UI элементов
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        
        // Обработчик кнопки регистрации
        btnRegister.setOnClickListener(v -> registerUser());
        
        // Обработчик перехода на экран входа
        tvLogin.setOnClickListener(v -> finish());
    }
    
    /**
     * Метод для регистрации пользователя
     */
    private void registerUser() {
        // Получение и проверка введенных данных
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        
        // Проверка заполнения всех полей
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Создаем имя пользователя из email
        String username = email.split("@")[0];
        
        // Текущая дата для дня рождения (можно будет изменить в профиле)
        String birthDate = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        
        // Регистрация пользователя в базе данных
        boolean registerSuccess = dataRepository.registerUser(email, password, fullName, username, birthDate);
        
        if (registerSuccess) {
            // Получаем данные пользователя
            Models.User user = dataRepository.getUser(email);
            if (user != null) {
                // Создаем сессию пользователя
                sessionManager.createLoginSession(user.getId(), email, user.getFullName());
                
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                
                // Переходим на главный экран
                Intent intent = new Intent(RegisterActivity.this, BaseActivity.class);
                intent.putExtra("screen_type", BaseActivity.SCREEN_BOOKS_LIST);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Ошибка регистрации. Возможно, пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
        }
    }
}