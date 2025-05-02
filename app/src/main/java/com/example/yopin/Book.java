package com.example.yopin;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private String year;
    private String firebaseId; // ID документа в Firebase
    private List<Review> reviews; // Список отзывов к книге
    private float averageRating; // Средний рейтинг книги
    private String coverImage; // Путь к обложке книги

    // Пустой конструктор для Firebase
    public Book() {
        this.reviews = new ArrayList<>();
    }

    public Book(int id, String title, String author, String genre, String year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.reviews = new ArrayList<>();
        this.averageRating = 0.0f;
    }

    // Конструктор с firebaseId
    public Book(int id, String title, String author, String genre, String year, String firebaseId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.firebaseId = firebaseId;
        this.reviews = new ArrayList<>();
        this.averageRating = 0.0f;
    }

    // Полный конструктор
    public Book(int id, String title, String author, String genre, String year, String firebaseId, 
                List<Review> reviews, float averageRating, String coverImage) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.firebaseId = firebaseId;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        this.averageRating = averageRating;
        this.coverImage = coverImage;
    }

    // Геттеры
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getYear() { return year; }
    public String getFirebaseId() { return firebaseId; }
    public List<Review> getReviews() { return reviews; }
    public float getAverageRating() { return averageRating; }
    public String getCoverImage() { return coverImage; }
    
    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setYear(String year) { this.year = year; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    // Метод добавления отзыва и обновления среднего рейтинга
    public void addReview(Review review) {
        if (this.reviews == null) {
            this.reviews = new ArrayList<>();
        }
        this.reviews.add(review);
        updateAverageRating();
    }
    
    // Метод обновления среднего рейтинга
    public void updateAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            this.averageRating = 0.0f;
            return;
        }
        
        float totalRating = 0.0f;
        int count = 0;
        
        for (Review review : reviews) {
            if (review.getRating() > 0) {
                totalRating += review.getRating();
                count++;
            }
        }
        
        this.averageRating = count > 0 ? totalRating / count : 0.0f;
    }
    
    // Метод для изящного форматирования информации о книге
    public String getFormattedInfo() {
        StringBuilder info = new StringBuilder();
        info.append(title);
        
        if (author != null && !author.isEmpty()) {
            info.append(" - ").append(author);
        }
        
        if (genre != null && !genre.isEmpty()) {
            info.append(" (").append(genre).append(")");
        }
        
        if (year != null && !year.isEmpty()) {
            info.append(", ").append(year);
        }
        
        return info.toString();
    }
    
    // Метод для получения количества отзывов
    public int getReviewCount() {
        return reviews != null ? reviews.size() : 0;
    }
}