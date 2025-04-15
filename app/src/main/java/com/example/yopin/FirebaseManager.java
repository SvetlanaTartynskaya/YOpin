package com.example.yopin;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DatabaseHelper localDb;
    
    private FirebaseManager(Context context) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        localDb = new DatabaseHelper(context);
    }
    
    public static synchronized FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context.getApplicationContext());
        }
        return instance;
    }
    
    // Регистрация нового пользователя
    public void registerUser(String email, String password, String fullName, String username, String birthDate, OnAuthCompleteListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            
                            // Сохраняем данные пользователя в Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("fullName", fullName);
                            userData.put("username", username);
                            userData.put("birthDate", birthDate);
                            
                            db.collection("users").document(uid).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Также сохраняем в локальной базе
                                        localDb.addUser(email, password, fullName, username, birthDate);
                                        listener.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error saving user data", e);
                                        listener.onFailure(e.getMessage());
                                    });
                        }
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }
    
    // Вход пользователя
    public void loginUser(String email, String password, OnAuthCompleteListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                        // После успешного входа загружаем данные с сервера
                        syncDataFromServer();
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }
    
    // Выход пользователя
    public void logoutUser() {
        auth.signOut();
    }
    
    // Проверка текущего пользователя
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    
    // Загрузка данных с сервера
    public void syncDataFromServer() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        
        // Синхронизация книг
        db.collection("books").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String author = document.getString("author");
                            String genre = document.getString("genre");
                            String year = document.getString("year");
                            
                            if (title != null && author != null) {
                                localDb.addBook(title, author, genre != null ? genre : "", year != null ? year : "");
                            }
                        }
                    } else {
                        Log.e(TAG, "Error syncing books", task.getException());
                    }
                });
        
        // Синхронизация отзывов пользователя
        db.collection("reviews")
                .whereEqualTo("userEmail", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int userId = document.getLong("userId") != null ? document.getLong("userId").intValue() : -1;
                            int bookId = document.getLong("bookId") != null ? document.getLong("bookId").intValue() : -1;
                            String reviewText = document.getString("reviewText");
                            int rating = document.getLong("rating") != null ? document.getLong("rating").intValue() : 0;
                            String date = document.getString("date");
                            
                            if (userId != -1 && reviewText != null && date != null) {
                                if (bookId != -1) {
                                    localDb.addReview(userId, bookId, reviewText, rating, date);
                                } else {
                                    localDb.addReview(userId, reviewText, date);
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Error syncing reviews", task.getException());
                    }
                });
    }
    
    // Добавить книгу и синхронизировать с сервером
    public void addBook(String title, String author, String genre, String year, OnDataSyncListener listener) {
        // Сначала добавляем в локальную базу
        long bookId = localDb.addBook(title, author, genre, year);
        
        // Затем добавляем в облако
        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", title);
        bookData.put("author", author);
        bookData.put("genre", genre);
        bookData.put("year", year);
        
        db.collection("books").add(bookData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Book added with ID: " + documentReference.getId());
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding book", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    
    // Добавить отзыв и синхронизировать с сервером
    public void addReview(int userId, int bookId, String reviewText, int rating, String date, OnDataSyncListener listener) {
        // Сначала добавляем в локальную базу
        boolean success = localDb.addReview(userId, bookId, reviewText, rating, date);
        
        // Затем добавляем в облако
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (listener != null) listener.onFailure("User not logged in");
            return;
        }
        
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", userId);
        reviewData.put("bookId", bookId);
        reviewData.put("reviewText", reviewText);
        reviewData.put("rating", rating);
        reviewData.put("date", date);
        reviewData.put("userEmail", user.getEmail());
        
        db.collection("reviews").add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Review added with ID: " + documentReference.getId());
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding review", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    
    // Добавить общий отзыв и синхронизировать с сервером
    public void addReview(int userId, String reviewText, String date, OnDataSyncListener listener) {
        // Сначала добавляем в локальную базу
        boolean success = localDb.addReview(userId, reviewText, date);
        
        // Затем добавляем в облако
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (listener != null) listener.onFailure("User not logged in");
            return;
        }
        
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", userId);
        reviewData.put("reviewText", reviewText);
        reviewData.put("date", date);
        reviewData.put("userEmail", user.getEmail());
        
        db.collection("reviews").add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "General review added with ID: " + documentReference.getId());
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding general review", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    
    // Обновить отзыв и синхронизировать с сервером
    public void updateReview(int reviewId, String reviewText, int rating, OnDataSyncListener listener) {
        // Сначала обновляем в локальной базе
        boolean success = localDb.updateReview(reviewId, reviewText, rating);
        
        // Для обновления в облаке сначала нужно найти документ по reviewId
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (listener != null) listener.onFailure("User not logged in");
            return;
        }
        
        // Получаем отзыв из локальной базы
        Review review = localDb.getReviewById(reviewId);
        if (review == null) {
            if (listener != null) listener.onFailure("Review not found");
            return;
        }
        
        db.collection("reviews")
                .whereEqualTo("userEmail", user.getEmail())
                .whereEqualTo("userId", review.getUserId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String docReviewText = document.getString("reviewText");
                            String docDate = document.getString("date");
                            
                            // Найдем документ по дате и тексту (примерное совпадение)
                            if (docDate != null && docDate.equals(review.getDate())) {
                                // Обновляем
                                document.getReference().update("reviewText", reviewText, "rating", rating)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Review updated with ID: " + document.getId());
                                            if (listener != null) listener.onSuccess();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error updating review", e);
                                            if (listener != null) listener.onFailure(e.getMessage());
                                        });
                                return;
                            }
                        }
                        // Если не нашли документ, добавляем новый
                        if (listener != null) listener.onFailure("Review not found in cloud");
                    } else {
                        Log.e(TAG, "Error finding review", task.getException());
                        if (listener != null) listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }
    
    // Интерфейс для обратных вызовов аутентификации
    public interface OnAuthCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
    
    // Интерфейс для обратных вызовов синхронизации данных
    public interface OnDataSyncListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
} 