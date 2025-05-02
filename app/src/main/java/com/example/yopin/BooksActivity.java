package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class BooksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BooksAdapter adapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAddBook;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        
        // Настройка RecyclerView для книг
        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Загружаем книги
        loadBooks();
        
        // Настройка кнопки добавления книги
        fabAddBook = findViewById(R.id.fabAddBook);
        fabAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(BooksActivity.this, AddBookActivity.class);
            startActivity(intent);
        });
        
        // Настройка навигационных кнопок
        findViewById(R.id.btnMyReviews).setOnClickListener(v -> {
            Intent intent = new Intent(BooksActivity.this, UserReviewsActivity.class);
            intent.putExtra("user_id", sessionManager.getUserId());
            startActivity(intent);
        });
        
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            Intent intent = new Intent(BooksActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список книг при возвращении к активности
        loadBooks();
    }
    
    private void loadBooks() {
        // Получаем список книг из базы данных
        List<Book> books = dbHelper.getAllBooks();
        
        // Создаем адаптер и устанавливаем его для RecyclerView
        adapter = new BooksAdapter(books, this);
        recyclerView.setAdapter(adapter);
    }
} 