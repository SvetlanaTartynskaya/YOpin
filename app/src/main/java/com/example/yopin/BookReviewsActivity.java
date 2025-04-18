package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookReviewsActivity extends AppCompatActivity {
    private TextView tvBookTitle, tvBookAuthor, tvBookGenre, tvBookYear;
    private Button btnAddReview;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter adapter;
    private DatabaseHelper dbHelper;
    private int bookId;
    private String userEmail;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reviews);

        dbHelper = new DatabaseHelper(this);

        // Получаем id книги и email пользователя из Intent
        bookId = getIntent().getIntExtra("book_id", -1);
        userEmail = getIntent().getStringExtra("email");

        if (bookId == -1 || userEmail == null) {
            Toast.makeText(this, "Ошибка: информация о книге не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Получаем информацию о текущем пользователе
        currentUser = dbHelper.getUser(userEmail);
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация UI элементов
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvBookAuthor = findViewById(R.id.tvBookAuthor);
        tvBookGenre = findViewById(R.id.tvBookGenre);
        tvBookYear = findViewById(R.id.tvBookYear);
        btnAddReview = findViewById(R.id.btnAddReview);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));

        // Загружаем информацию о книге
        loadBookDetails();

        // Загружаем отзывы о книге
        loadReviews();

        // Проверяем, оставлял ли пользователь отзыв на эту книгу
        boolean hasReviewed = dbHelper.hasUserReviewedBook(currentUser.getId(), bookId);
        if (hasReviewed) {
            btnAddReview.setText("Вы уже оставили отзыв");
            btnAddReview.setEnabled(false);
        } else {
            btnAddReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BookReviewsActivity.this, AddReviewActivity.class);
                    intent.putExtra("book_id", bookId);
                    intent.putExtra("user_id", currentUser.getId());
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем отзывы и проверяем, не добавил ли пользователь отзыв
        loadReviews();
        boolean hasReviewed = dbHelper.hasUserReviewedBook(currentUser.getId(), bookId);
        if (hasReviewed) {
            btnAddReview.setText("Вы уже оставили отзыв");
            btnAddReview.setEnabled(false);
        }
    }

    private void loadBookDetails() {
        Book book = dbHelper.getBookById(bookId);
        if (book != null) {
            tvBookTitle.setText(book.getTitle());
            tvBookAuthor.setText(book.getAuthor());
            tvBookGenre.setText("Жанр: " + book.getGenre());
            tvBookYear.setText("Год: " + book.getYear());
        } else {
            Toast.makeText(this, "Ошибка загрузки информации о книге", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadReviews() {
        List<Review> reviews = dbHelper.getReviewsForBook(bookId);
        adapter = new ReviewAdapter(reviews, this, currentUser.getId());
        recyclerViewReviews.setAdapter(adapter);

        if (reviews.isEmpty()) {
            findViewById(R.id.tvNoReviews).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tvNoReviews).setVisibility(View.GONE);
        }
    }
}