package com.example.yopin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WriteReviewActivity extends AppCompatActivity {
    private EditText etReviewText;
    private Button btnSubmitReview;
    private DatabaseHelper dbHelper;
    private FirebaseManager firebaseManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        dbHelper = new DatabaseHelper(this);
        firebaseManager = FirebaseManager.getInstance(this);
        
        // Получаем id пользователя из Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etReviewText = findViewById(R.id.etReviewText);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });
    }

    private void submitReview() {
        String reviewText = etReviewText.getText().toString().trim();
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Напишите текст отзыва", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем текущую дату
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Сохраняем в Firebase и локальной базе данных
        firebaseManager.addReview(userId, reviewText, currentDate, new FirebaseManager.OnDataSyncListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WriteReviewActivity.this, "Отзыв успешно сохранен", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Если Firebase добавление не удалось, пробуем только локальную базу
                boolean success = dbHelper.addReview(userId, reviewText, currentDate);
                if (success) {
                    Toast.makeText(WriteReviewActivity.this, "Отзыв сохранен (оффлайн режим)", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(WriteReviewActivity.this, "Ошибка при сохранении отзыва", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
} 