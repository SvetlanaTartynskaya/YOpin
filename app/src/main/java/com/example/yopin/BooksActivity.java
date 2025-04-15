package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class BooksActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAddBook;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        
        dbHelper = new DatabaseHelper(this);
        
        userEmail = getIntent().getStringExtra("email");
        
        // Настраиваем RecyclerView для отображения списка книг
        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Кнопка для добавления новой книги
        fabAddBook = findViewById(R.id.fabAddBook);
        fabAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BooksActivity.this, AddBookActivity.class);
                intent.putExtra("email", userEmail);
                startActivity(intent);
            }
        });
        
        // Загружаем список книг
        loadBooks();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список книг при возвращении на экран
        loadBooks();
    }
    
    private void loadBooks() {
        List<Book> books = dbHelper.getAllBooks();
        User currentUser = dbHelper.getUser(userEmail);
        int userId = currentUser != null ? currentUser.getId() : -1;
        adapter = new BookAdapter(books, this, this, userId);
        recyclerView.setAdapter(adapter);
    }
    
    @Override
    public void onBookClick(int bookId) {
        // Переходим на экран отзывов о выбранной книге
        Intent intent = new Intent(BooksActivity.this, BookReviewsActivity.class);
        intent.putExtra("book_id", bookId);
        intent.putExtra("email", userEmail);
        startActivity(intent);
    }
    
    // Геттер для email пользователя
    public String getUserEmail() {
        return userEmail;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_books, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_profile) {
            // Переход в профиль пользователя
            Intent intent = new Intent(BooksActivity.this, ProfileActivity.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_my_reviews) {
            // Переход к списку отзывов пользователя
            Intent intent = new Intent(BooksActivity.this, UserReviewsActivity.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 