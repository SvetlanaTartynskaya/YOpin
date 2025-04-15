package com.example.yopin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvFullName, tvEmail, tvBirthDate;
    private Button btnEditBirthDate, btnWriteReview, btnViewBooks, btnMyReviews;
    private DatabaseHelper dbHelper;
    private User currentUser;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();

        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        btnEditBirthDate = findViewById(R.id.btnEditBirthDate);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        btnViewBooks = findViewById(R.id.btnViewBooks);
        btnMyReviews = findViewById(R.id.btnMyReviews);

        // Получаем email из Intent
        String email = getIntent().getStringExtra("email");
        if (email == null) {
            Toast.makeText(this, "Ошибка: пользователь не определен", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Загружаем данные пользователя
        currentUser = dbHelper.getUser(email);
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayUserInfo();

        btnEditBirthDate.setOnClickListener(v -> showDatePicker());
        btnWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WriteReviewActivity.class);
            intent.putExtra("user_id", currentUser.getId());
            startActivity(intent);
        });

        // Обработчик кнопки "Просмотр книг"
        btnViewBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, BooksActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Мои отзывы"
        btnMyReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    Intent intent = new Intent(ProfileActivity.this, UserReviewsActivity.class);
                    intent.putExtra("user_id", currentUser.getId());
                    intent.putExtra("email", email);
                    startActivity(intent);
                }
            }
        });
    }

    private void displayUserInfo() {
        tvFullName.setText(currentUser.getFullName());
        tvEmail.setText(currentUser.getEmail());
        tvBirthDate.setText(currentUser.getBirthDate());
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    String selectedDate = sdf.format(calendar.getTime());

                    // Обновляем дату в базе данных
                    currentUser.setBirthDate(selectedDate);
                    dbHelper.updateUserBirthDate(currentUser.getId(), selectedDate);
                    tvBirthDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
}