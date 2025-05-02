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
        
        Log.d(TAG, "Начинаем синхронизацию данных с сервера");
        
        // Сначала синхронизируем книги
        db.collection("books").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Успешно получено " + task.getResult().size() + " книг с сервера");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            String title = document.getString("title");
                            String author = document.getString("author");
                            String genre = document.getString("genre");
                            String year = document.getString("year");
                            Long localId = document.getLong("localId");
                            Double averageRating = document.getDouble("averageRating");
                            Long reviewCount = document.getLong("reviewCount");
                            
                            if (title != null && author != null) {
                                // Сохраняем книгу с ID документа Firebase
                                long newLocalId;
                                
                                if (localId != null) {
                                    // Если есть localId, проверяем существует ли книга с таким ID
                                    Book existingBook = localDb.getBookById(localId.intValue());
                                    if (existingBook != null) {
                                        // Обновляем существующую книгу
                                        localDb.updateBookFirebaseId(localId.intValue(), documentId);
                                        newLocalId = localId;
                                    } else {
                                        // Книги с таким ID нет, создаем новую
                                        newLocalId = localDb.addBookWithFirebaseId(title, author, 
                                                genre != null ? genre : "", 
                                                year != null ? year : "", 
                                                documentId);
                                    }
                                } else {
                                    // Нет localId, просто добавляем новую книгу
                                    newLocalId = localDb.addBookWithFirebaseId(title, author, 
                                            genre != null ? genre : "", 
                                            year != null ? year : "", 
                                            documentId);
                                }
                                
                                Log.d(TAG, "Synced book: " + title + " with Firebase ID: " + documentId + ", local ID: " + newLocalId);
                                
                                // Также синхронизируем отзывы для этой книги сразу
                                syncReviewsForBook(documentId, (int)newLocalId);
                            }
                        }
                        
                        // После синхронизации книг синхронизируем отзывы пользователя
                        syncUserReviews(user);
                    } else {
                        Log.e(TAG, "Error syncing books", task.getException());
                        // Всё равно пытаемся синхронизировать отзывы пользователя
                        syncUserReviews(user);
                    }
                });
    }
    
    // Синхронизация отзывов конкретной книги
    private void syncReviewsForBook(String bookFirebaseId, int localBookId) {
        db.collection("books").document(bookFirebaseId)
          .collection("reviews").get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  int syncedReviews = 0;
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      // Пропускаем документ с статистикой
                      if (document.getId().equals("stats")) continue;
                      
                      int userId = document.getLong("userId") != null ? document.getLong("userId").intValue() : -1;
                      String reviewText = document.getString("reviewText");
                      int rating = document.getLong("rating") != null ? document.getLong("rating").intValue() : 0;
                      String date = document.getString("date");
                      String userEmail = document.getString("userEmail");
                      String username = document.getString("username");
                      
                      if (userId != -1 && reviewText != null && date != null) {
                          // Если пользователь не существует в локальной БД, создаем временного
                          if (!localDb.userExists(userId) && userEmail != null) {
                              localDb.addUser(userEmail, "", username != null ? username : "User_" + userId, username != null ? username : "User_" + userId, "");
                          }
                          
                          boolean added = localDb.addReview(userId, localBookId, reviewText, rating, date);
                          if (added) syncedReviews++;
                      }
                  }
                  Log.d(TAG, "Synchronized " + syncedReviews + " reviews for book ID: " + bookFirebaseId);
              } else {
                  Log.e(TAG, "Error syncing reviews for book: " + bookFirebaseId, task.getException());
              }
          });
    }
    
    // Синхронизация отзывов пользователя
    private void syncUserReviews(FirebaseUser user) {
        db.collection("reviews")
                .whereEqualTo("userEmail", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Успешно получено " + task.getResult().size() + " отзывов пользователя с сервера");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int userId = document.getLong("userId") != null ? document.getLong("userId").intValue() : -1;
                            int bookId = document.getLong("bookId") != null ? document.getLong("bookId").intValue() : -1;
                            String bookFirebaseId = document.getString("bookFirebaseId");
                            String reviewText = document.getString("reviewText");
                            int rating = document.getLong("rating") != null ? document.getLong("rating").intValue() : 0;
                            String date = document.getString("date");
                            
                            // Если есть Firebase ID книги, используем его для поиска локального ID
                            if (bookFirebaseId != null && !bookFirebaseId.isEmpty()) {
                                Book book = localDb.getBookByFirebaseId(bookFirebaseId);
                                if (book != null) {
                                    bookId = book.getId();
                                }
                            }
                            
                            if (userId != -1 && reviewText != null && date != null) {
                                if (bookId != -1) {
                                    localDb.addReview(userId, bookId, reviewText, rating, date);
                                    Log.d(TAG, "Synced review for book ID: " + bookId);
                                } else {
                                    localDb.addReview(userId, reviewText, date);
                                    Log.d(TAG, "Synced general review");
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Error syncing user reviews", task.getException());
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
        bookData.put("localId", bookId); // Сохраняем локальный ID для связи
        bookData.put("createdAt", System.currentTimeMillis()); // Добавляем временную метку создания
        bookData.put("averageRating", 0.0); // Начальный средний рейтинг
        bookData.put("reviewCount", 0); // Количество отзывов
        
        db.collection("books").add(bookData)
                .addOnSuccessListener(documentReference -> {
                    String firebaseId = documentReference.getId();
                    Log.d(TAG, "Book added with ID: " + firebaseId);
                    
                    // Обновляем локальную запись, чтобы сохранить ID документа Firebase
                    localDb.updateBookFirebaseId((int)bookId, firebaseId);
                    
                    // Создаем подколлекцию reviews для этой книги
                    db.collection("books").document(firebaseId)
                      .collection("reviews").document("stats")
                      .set(new HashMap<String, Object>() {{
                          put("count", 0);
                          put("totalRating", 0);
                      }});
                    
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding book", e);
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }
    
    // Добавить отзыв и синхронизировать с сервером
    public void addReview(int userId, int bookId, String reviewText, int rating, String date, OnDataSyncListener listener) {
        // Сначала проверим, существует ли книга в Firebase
        String bookFirebaseId = localDb.getBookFirebaseId(bookId);
        
        if (bookFirebaseId == null || bookFirebaseId.isEmpty()) {
            // Книга не имеет Firebase ID, значит она еще не синхронизирована с Firebase
            // Сначала получим данные о книге
            Book book = localDb.getBookById(bookId);
            if (book != null) {
                // Синхронизируем книгу с Firebase
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("title", book.getTitle());
                bookData.put("author", book.getAuthor());
                bookData.put("genre", book.getGenre());
                bookData.put("year", book.getYear());
                bookData.put("localId", book.getId());
                bookData.put("createdAt", System.currentTimeMillis());
                bookData.put("averageRating", 0.0);
                bookData.put("reviewCount", 0);
                
                db.collection("books").add(bookData)
                        .addOnSuccessListener(documentReference -> {
                            String firebaseId = documentReference.getId();
                            Log.d(TAG, "Book synced with ID: " + firebaseId);
                            
                            // Обновляем локальную запись с firebase id
                            localDb.updateBookFirebaseId(bookId, firebaseId);
                            
                            // Создаем подколлекцию reviews для этой книги
                            db.collection("books").document(firebaseId)
                              .collection("reviews").document("stats")
                              .set(new HashMap<String, Object>() {{
                                  put("count", 0);
                                  put("totalRating", 0);
                              }});
                            
                            // Теперь добавляем отзыв
                            addReviewAfterBookSync(userId, bookId, reviewText, rating, date, firebaseId, listener);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error syncing book", e);
                            // Добавляем отзыв без Firebase ID книги
                            addReviewAfterBookSync(userId, bookId, reviewText, rating, date, null, listener);
                        });
            } else {
                // Книга не найдена в локальной базе, просто добавляем отзыв
                addReviewAfterBookSync(userId, bookId, reviewText, rating, date, null, listener);
            }
        } else {
            // Книга уже синхронизирована с Firebase, добавляем отзыв
            addReviewAfterBookSync(userId, bookId, reviewText, rating, date, bookFirebaseId, listener);
        }
    }
    
    // Вспомогательный метод для добавления отзыва после синхронизации книги
    private void addReviewAfterBookSync(int userId, int bookId, String reviewText, int rating, String date, 
                                      String bookFirebaseId, OnDataSyncListener listener) {
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
        reviewData.put("createdAt", System.currentTimeMillis());
        
        // Получаем информацию о пользователе для сохранения в отзыве
        String username = localDb.getUsernameById(userId);
        if (username != null && !username.isEmpty()) {
            reviewData.put("username", username);
        }
        
        // Если есть Firebase ID книги, добавляем отзыв как подколлекцию к книге
        if (bookFirebaseId != null && !bookFirebaseId.isEmpty()) {
            reviewData.put("bookFirebaseId", bookFirebaseId);
            
            // Добавляем отзыв как в основную коллекцию reviews, так и в подколлекцию книги
            db.collection("books").document(bookFirebaseId)
              .collection("reviews").add(reviewData)
              .addOnSuccessListener(reviewDocRef -> {
                  String reviewId = reviewDocRef.getId();
                  Log.d(TAG, "Review added to book with ID: " + reviewId);
                  
                  // Обновляем счетчик отзывов и средний рейтинг для книги
                  db.collection("books").document(bookFirebaseId).get()
                    .addOnSuccessListener(bookDocSnapshot -> {
                        double currentAvgRating = bookDocSnapshot.getDouble("averageRating") != null ? 
                                                  bookDocSnapshot.getDouble("averageRating") : 0.0;
                        long currentReviewCount = bookDocSnapshot.getLong("reviewCount") != null ? 
                                                 bookDocSnapshot.getLong("reviewCount") : 0;
                        
                        // Вычисляем новый средний рейтинг
                        double totalRating = currentAvgRating * currentReviewCount + rating;
                        currentReviewCount++;
                        double newAvgRating = totalRating / currentReviewCount;
                        
                        // Обновляем данные книги
                        Map<String, Object> bookUpdate = new HashMap<>();
                        bookUpdate.put("averageRating", newAvgRating);
                        bookUpdate.put("reviewCount", currentReviewCount);
                        
                        db.collection("books").document(bookFirebaseId)
                          .update(bookUpdate);
                        
                        // Обновляем stats в подколлекции отзывов
                        db.collection("books").document(bookFirebaseId)
                          .collection("reviews").document("stats")
                          .update("count", currentReviewCount, 
                                  "totalRating", totalRating);
                    });
                  
                  // Добавляем также в основную коллекцию отзывов
                  db.collection("reviews").add(reviewData)
                    .addOnSuccessListener(mainReviewDocRef -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding review to main collection", e);
                        // Отзыв уже добавлен к книге, поэтому считаем успешным
                        if (listener != null) listener.onSuccess();
                    });
              })
              .addOnFailureListener(e -> {
                  Log.e(TAG, "Error adding review to book", e);
                  
                  // Попробуем добавить хотя бы в основную коллекцию
                  db.collection("reviews").add(reviewData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Review added only to main collection: " + documentReference.getId());
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e2 -> {
                        Log.e(TAG, "Error adding review to main collection", e2);
                        if (listener != null) listener.onFailure(e2.getMessage());
                    });
              });
        } else {
            // Нет Firebase ID книги, добавляем только в основную коллекцию
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