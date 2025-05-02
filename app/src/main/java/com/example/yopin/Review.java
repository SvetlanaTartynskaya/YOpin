package com.example.yopin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Review {
    private int id;
    private int userId;
    private int bookId;
    private String reviewText;
    private int rating;
    private String date;
    private String username;
    private String bookTitle;
    private String bookAuthor;
    private String bookFirebaseId; // ID документа книги в Firebase
    private String reviewFirebaseId; // ID самого отзыва в Firebase

    // Пустой конструктор для Firebase
    public Review() {
    }

    // Базовый конструктор
    public Review(int id, int userId, int bookId, String reviewText, int rating, String date) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
    }

    // Конструктор для отзывов с информацией о пользователе
    public Review(int id, int userId, int bookId, String reviewText, int rating, String date, String username) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
        this.username = username;
    }

    // Конструктор для отзывов с информацией о книге
    public Review(int id, int userId, int bookId, String reviewText, int rating, String date, String bookTitle, String bookAuthor) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
    }

    // Полный конструктор
    public Review(int id, int userId, int bookId, String reviewText, int rating, String date, 
                  String username, String bookTitle, String bookAuthor, String bookFirebaseId, String reviewFirebaseId) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
        this.username = username;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookFirebaseId = bookFirebaseId;
        this.reviewFirebaseId = reviewFirebaseId;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getBookId() {
        return bookId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public int getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public String getUsername() {
        return username;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public String getBookFirebaseId() {
        return bookFirebaseId;
    }
    
    public String getReviewFirebaseId() {
        return reviewFirebaseId;
    }
    
    // Сеттеры
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    
    public void setBookFirebaseId(String bookFirebaseId) {
        this.bookFirebaseId = bookFirebaseId;
    }
    
    public void setReviewFirebaseId(String reviewFirebaseId) {
        this.reviewFirebaseId = reviewFirebaseId;
    }
    
    // Форматированное отображение даты
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
            Date parsedDate = inputFormat.parse(date);
            if (parsedDate != null) {
                return outputFormat.format(parsedDate);
            }
        } catch (Exception e) {
            // В случае ошибки возвращаем оригинальную дату
        }
        return date;
    }
    
    // Метод для отображения звездочек рейтинга в виде текста
    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
}