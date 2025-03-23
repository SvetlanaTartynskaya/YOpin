package com.example.yopin;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.yopin.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private EditText bookTitleEditText, reviewEditText;
    private Button postReviewButton;
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        bookTitleEditText = findViewById(R.id.bookTitleEditText);
        reviewEditText = findViewById(R.id.reviewEditText);
        postReviewButton = findViewById(R.id.postReviewButton);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(null);
        recyclerView.setAdapter(adapter);

        postReviewButton.setOnClickListener(v -> {
            String bookTitle = bookTitleEditText.getText().toString();
            String reviewText = reviewEditText.getText().toString();
            long userId = 1; // Здесь должен быть ID текущего пользователя

            long reviewId = dbHelper.addReview(bookTitle, reviewText, userId);
            if (reviewId != -1) {
                Toast.makeText(MainActivity.this, "Отзыв опубликован", Toast.LENGTH_SHORT).show();
                loadReviews();
            } else {
                Toast.makeText(MainActivity.this, "Ошибка публикации", Toast.LENGTH_SHORT).show();
            }
        });

        loadReviews();
    }

    private void loadReviews() {
        Cursor cursor = dbHelper.getAllReviews();
        adapter.swapCursor(cursor);
    }
}