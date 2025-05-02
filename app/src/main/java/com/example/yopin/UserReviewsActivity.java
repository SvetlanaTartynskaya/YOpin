package com.example.yopin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserReviewsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUserReviews;
    private UserReviewAdapter adapter;
    private DatabaseHelper dbHelper;
    private int userId;
    private TextView tvNoReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reviews);

        dbHelper = new DatabaseHelper(this);

        // Получаем id пользователя из Intent
        String email = getIntent().getStringExtra("email");
        userId = getIntent().getIntExtra("user_id", -1);

        // Если id не передан, получаем его по email
        if (userId == -1 && email != null) {
            User user = dbHelper.getUser(email);
            if (user != null) {
                userId = user.getId();
            }
        }

        if (userId == -1) {
            Toast.makeText(this, "Ошибка: информация о пользователе не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация UI элементов
        recyclerViewUserReviews = findViewById(R.id.recyclerViewUserReviews);
        recyclerViewUserReviews.setLayoutManager(new LinearLayoutManager(this));
        tvNoReviews = findViewById(R.id.tvNoReviews);

        // Загружаем отзывы пользователя
        loadUserReviews();
    }

    private void loadUserReviews() {
        List<Review> reviews = dbHelper.getUserReviews(userId);
        adapter = new UserReviewAdapter(reviews, this);
        recyclerViewUserReviews.setAdapter(adapter);

        if (reviews.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
        } else {
            tvNoReviews.setVisibility(View.GONE);
        }
    }
} 