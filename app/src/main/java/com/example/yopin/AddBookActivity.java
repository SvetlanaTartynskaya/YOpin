package com.example.yopin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddBookActivity extends AppCompatActivity {
    private EditText etBookTitle, etBookAuthor, etBookGenre, etBookYear;
    private Button btnSaveBook;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        firebaseManager = FirebaseManager.getInstance(this);

        // Инициализация UI элементов
        etBookTitle = findViewById(R.id.etBookTitle);
        etBookAuthor = findViewById(R.id.etBookAuthor);
        etBookGenre = findViewById(R.id.etBookGenre);
        etBookYear = findViewById(R.id.etBookYear);
        btnSaveBook = findViewById(R.id.btnSaveBook);

        btnSaveBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBook();
            }
        });
    }

    private void saveBook() {
        String title = etBookTitle.getText().toString().trim();
        String author = etBookAuthor.getText().toString().trim();
        String genre = etBookGenre.getText().toString().trim();
        String year = etBookYear.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "Название и автор должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        // Используем FirebaseManager для сохранения книги в локальной БД и Firebase
        firebaseManager.addBook(title, author, genre, year, new FirebaseManager.OnDataSyncListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddBookActivity.this, "Книга успешно добавлена и синхронизирована", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AddBookActivity.this, "Ошибка синхронизации: " + errorMessage, Toast.LENGTH_SHORT).show();
                // Книга всё равно сохранена локально, поэтому закрываем активность
                finish();
            }
        });
    }
} 