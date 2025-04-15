package com.example.yopin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditReviewActivity extends AppCompatActivity {
    private TextView tvBookInfo;
    private EditText etReviewText;
    private RatingBar ratingBar;
    private Button btnUpdateReview;
    private DatabaseHelper dbHelper;
    private FirebaseManager firebaseManager;
    private int reviewId;
    private Review currentReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_review);

        dbHelper = new DatabaseHelper(this);
        firebaseManager = FirebaseManager.getInstance(this);

        // Получаем id отзыва из Intent
        reviewId = getIntent().getIntExtra("review_id", -1);

        if (reviewId == -1) {
            Toast.makeText(this, "Ошибка: отзыв не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация UI элементов
        tvBookInfo = findViewById(R.id.tvBookInfo);
        etReviewText = findViewById(R.id.etReviewText);
        ratingBar = findViewById(R.id.ratingBar);
        btnUpdateReview = findViewById(R.id.btnUpdateReview);

        // Загружаем информацию об отзыве
        loadReviewInfo();

        btnUpdateReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReview();
            }
        });
    }

    private void loadReviewInfo() {
        currentReview = dbHelper.getReviewById(reviewId);
        if (currentReview != null) {
            // Заполняем поля отзыва
            etReviewText.setText(currentReview.getReviewText());
            
            // Если отзыв о книге
            if (currentReview.getBookId() > 0) {
                tvBookInfo.setText(currentReview.getBookTitle() + " - " + currentReview.getBookAuthor());
                tvBookInfo.setVisibility(View.VISIBLE);
                
                // Установка рейтинга
                ratingBar.setRating(currentReview.getRating());
                ratingBar.setVisibility(View.VISIBLE);
            } else {
                // Это общий отзыв
                tvBookInfo.setVisibility(View.GONE);
                ratingBar.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Ошибка загрузки отзыва", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateReview() {
        String reviewText = etReviewText.getText().toString().trim();
        int rating = (int) ratingBar.getRating();

        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, напишите текст отзыва", Toast.LENGTH_SHORT).show();
            return;
        }

        // Обновляем в Firebase и локальной базе данных
        firebaseManager.updateReview(reviewId, reviewText, rating, new FirebaseManager.OnDataSyncListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditReviewActivity.this, "Отзыв успешно обновлен", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Если Firebase обновление не удалось, пробуем только локальную базу
                boolean success = dbHelper.updateReview(reviewId, reviewText, rating);
                if (success) {
                    Toast.makeText(EditReviewActivity.this, "Отзыв обновлен (оффлайн режим)", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditReviewActivity.this, "Ошибка при обновлении отзыва", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
} 