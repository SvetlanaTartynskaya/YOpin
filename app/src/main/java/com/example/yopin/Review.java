package com.example.yopin;

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
}