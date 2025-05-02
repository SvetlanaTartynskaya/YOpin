package com.example.yopin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddReviewActivity extends AppCompatActivity {
    private TextView tvBookInfo;
    private EditText etReviewText;
    private RatingBar ratingBar;
    private Button btnSubmitReview;
    private DatabaseHelper dbHelper;
    private FirebaseManager firebaseManager;
    private int bookId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        dbHelper = new DatabaseHelper(this);
        firebaseManager = FirebaseManager.getInstance(this);

        // Получаем id книги и пользователя из Intent
        bookId = getIntent().getIntExtra("book_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);

        if (bookId == -1 || userId == -1) {
            Toast.makeText(this, "Ошибка: информация о книге или пользователе не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация UI элементов
        tvBookInfo = findViewById(R.id.tvBookInfo);
        etReviewText = findViewById(R.id.etReviewText);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        // Загружаем информацию о книге
        loadBookInfo();

        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReview();
            }
        });
    }

    private void loadBookInfo() {
        Book book = dbHelper.getBookById(bookId);
        if (book != null) {
            tvBookInfo.setText(book.getTitle() + " - " + book.getAuthor());
        } else {
            Toast.makeText(this, "Ошибка загрузки информации о книге", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveReview() {
        String reviewText = etReviewText.getText().toString().trim();
        int rating = ratingBar.getRating() > 0 ? (int)ratingBar.getRating() : 0;
        
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Напишите текст отзыва", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (userId == -1) {
            Toast.makeText(this, "Ошибка: Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (bookId == -1) {
            Toast.makeText(this, "Ошибка: Книга не найдена", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Текущая дата
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // Используем FirebaseManager для сохранения отзыва
        firebaseManager.addReview(userId, bookId, reviewText, rating, date, new FirebaseManager.OnDataSyncListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddReviewActivity.this, "Отзыв успешно добавлен", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AddReviewActivity.this, "Ошибка синхронизации: " + errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}