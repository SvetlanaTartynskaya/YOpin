package com.example.yopin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDB";
    private static final int DATABASE_VERSION = 1;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_BIRTH_DATE = "birth_date";

    // Таблица книг
    private static final String TABLE_BOOKS = "books";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_BOOK_TITLE = "title";
    private static final String COLUMN_BOOK_AUTHOR = "author";
    private static final String COLUMN_BOOK_GENRE = "genre";
    private static final String COLUMN_BOOK_YEAR = "year";

    // Таблица отзывов
    private static final String TABLE_REVIEWS = "reviews";
    private static final String COLUMN_REVIEW_ID = "review_id";
    private static final String COLUMN_REVIEW_TEXT = "review_text";
    private static final String COLUMN_REVIEW_DATE = "review_date";
    private static final String COLUMN_RATING = "rating";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Таблица пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_FULL_NAME + " TEXT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_BIRTH_DATE + " TEXT" + ")";

        // Таблица книг
        String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BOOK_TITLE + " TEXT,"
                + COLUMN_BOOK_AUTHOR + " TEXT,"
                + COLUMN_BOOK_GENRE + " TEXT,"
                + COLUMN_BOOK_YEAR + " TEXT,"
                + "UNIQUE(" + COLUMN_BOOK_TITLE + ", " + COLUMN_BOOK_AUTHOR + ")" + ")";

        // Таблица отзывов
        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + "("
                + COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_BOOK_ID + " INTEGER,"
                + COLUMN_REVIEW_TEXT + " TEXT,"
                + COLUMN_REVIEW_DATE + " TEXT,"
                + COLUMN_RATING + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_BOOK_ID + ")" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_BOOKS_TABLE);
        db.execSQL(CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }

    // Методы для работы с пользователями
    public boolean addUser(String email, String password, String fullName, String username, String birthDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_BIRTH_DATE, birthDate);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID, COLUMN_EMAIL, COLUMN_FULL_NAME, COLUMN_BIRTH_DATE};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = null;
        User user = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                int fullNameIndex = cursor.getColumnIndex(COLUMN_FULL_NAME);
                int birthDateIndex = cursor.getColumnIndex(COLUMN_BIRTH_DATE);

                if (idIndex >= 0 && emailIndex >= 0 && fullNameIndex >= 0 && birthDateIndex >= 0) {
                    user = new User(
                            cursor.getInt(idIndex),
                            cursor.getString(emailIndex),
                            cursor.getString(fullNameIndex),
                            cursor.getString(birthDateIndex)
                    );
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return user;
    }

    // Метод для обновления даты рождения пользователя
    public boolean updateUserBirthDate(int userId, String birthDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BIRTH_DATE, birthDate);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    // Методы для работы с книгами
    public long addBook(String title, String author, String genre, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_TITLE, title);
        values.put(COLUMN_BOOK_AUTHOR, author);
        values.put(COLUMN_BOOK_GENRE, genre);
        values.put(COLUMN_BOOK_YEAR, year);

        // Проверяем, существует ли уже такая книга
        long bookId = -1;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BOOKS,
                    new String[]{COLUMN_BOOK_ID},
                    COLUMN_BOOK_TITLE + " = ? AND " + COLUMN_BOOK_AUTHOR + " = ?",
                    new String[]{title, author}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                if (columnIndex >= 0) {
                    bookId = cursor.getLong(columnIndex);
                }
            } else {
                bookId = db.insert(TABLE_BOOKS, null, values);
            }
            return bookId;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public Book getBookById(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_BOOKS,
                    new String[]{COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, COLUMN_BOOK_GENRE, COLUMN_BOOK_YEAR},
                    COLUMN_BOOK_ID + " = ?",
                    new String[]{String.valueOf(bookId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
                int genreIndex = cursor.getColumnIndex(COLUMN_BOOK_GENRE);
                int yearIndex = cursor.getColumnIndex(COLUMN_BOOK_YEAR);

                if (idIndex >= 0 && titleIndex >= 0 && authorIndex >= 0 && genreIndex >= 0 && yearIndex >= 0) {
                    Book book = new Book(
                            cursor.getInt(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(authorIndex),
                            cursor.getString(genreIndex),
                            cursor.getString(yearIndex)
                    );
                    return book;
                }
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + COLUMN_BOOK_TITLE;
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
                int genreIndex = cursor.getColumnIndex(COLUMN_BOOK_GENRE);
                int yearIndex = cursor.getColumnIndex(COLUMN_BOOK_YEAR);

                do {
                    if (idIndex >= 0 && titleIndex >= 0 && authorIndex >= 0 && genreIndex >= 0 && yearIndex >= 0) {
                        Book book = new Book(
                                cursor.getInt(idIndex),
                                cursor.getString(titleIndex),
                                cursor.getString(authorIndex),
                                cursor.getString(genreIndex),
                                cursor.getString(yearIndex)
                        );
                        books.add(book);
                    }
                } while (cursor.moveToNext());
            }
            return books;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // Методы для работы с отзывами
    public boolean addReview(int userId, int bookId, String reviewText, int rating, String reviewDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_BOOK_ID, bookId);
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_RATING, rating);
        values.put(COLUMN_REVIEW_DATE, reviewDate);

        long result = db.insert(TABLE_REVIEWS, null, values);
        db.close();
        return result != -1;
    }

    // Перегруженный метод для обратной совместимости
    public boolean addReview(int userId, String reviewText, String reviewDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_REVIEW_DATE, reviewDate);

        long result = db.insert(TABLE_REVIEWS, null, values);
        db.close();
        return result != -1;
    }

    public List<Review> getReviewsForBook(int bookId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT r.*, u." + COLUMN_USERNAME +
                    " FROM " + TABLE_REVIEWS + " r INNER JOIN " + TABLE_USERS + " u ON r." + COLUMN_USER_ID + " = u." + COLUMN_USER_ID +
                    " WHERE r." + COLUMN_BOOK_ID + " = ?" +
                    " ORDER BY r." + COLUMN_REVIEW_DATE + " DESC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(bookId)});

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_REVIEW_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                int bookIdIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int textIndex = cursor.getColumnIndex(COLUMN_REVIEW_TEXT);
                int ratingIndex = cursor.getColumnIndex(COLUMN_RATING);
                int dateIndex = cursor.getColumnIndex(COLUMN_REVIEW_DATE);
                int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);

                do {
                    if (idIndex >= 0 && userIdIndex >= 0 && bookIdIndex >= 0 &&
                            textIndex >= 0 && dateIndex >= 0 && usernameIndex >= 0) {
                        
                        int rating = 0;
                        if (ratingIndex >= 0) {
                            rating = cursor.getInt(ratingIndex);
                        }

                        Review review = new Review(
                                cursor.getInt(idIndex),
                                cursor.getInt(userIdIndex),
                                cursor.getInt(bookIdIndex),
                                cursor.getString(textIndex),
                                rating,
                                cursor.getString(dateIndex),
                                cursor.getString(usernameIndex)
                        );
                        reviews.add(review);
                    }
                } while (cursor.moveToNext());
            }
            return reviews;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public List<Review> getUserReviews(int userId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT r.*, b." + COLUMN_BOOK_TITLE + ", b." + COLUMN_BOOK_AUTHOR +
                    " FROM " + TABLE_REVIEWS + " r LEFT JOIN " + TABLE_BOOKS + " b ON r." + COLUMN_BOOK_ID + " = b." + COLUMN_BOOK_ID +
                    " WHERE r." + COLUMN_USER_ID + " = ?" +
                    " ORDER BY r." + COLUMN_REVIEW_DATE + " DESC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_REVIEW_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                int bookIdIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int textIndex = cursor.getColumnIndex(COLUMN_REVIEW_TEXT);
                int ratingIndex = cursor.getColumnIndex(COLUMN_RATING);
                int dateIndex = cursor.getColumnIndex(COLUMN_REVIEW_DATE);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);

                do {
                    if (idIndex >= 0 && userIdIndex >= 0 && textIndex >= 0 && dateIndex >= 0) {
                        int bookId = -1;
                        if (bookIdIndex >= 0) {
                            bookId = cursor.getInt(bookIdIndex);
                        }
                        
                        int rating = 0;
                        if (ratingIndex >= 0) {
                            rating = cursor.getInt(ratingIndex);
                        }
                        
                        String bookTitle = "Общий отзыв";
                        String bookAuthor = "";
                        if (titleIndex >= 0 && bookId > 0) {
                            bookTitle = cursor.getString(titleIndex);
                        }
                        if (authorIndex >= 0 && bookId > 0) {
                            bookAuthor = cursor.getString(authorIndex);
                        }

                        Review review = new Review(
                                cursor.getInt(idIndex),
                                cursor.getInt(userIdIndex),
                                bookId,
                                cursor.getString(textIndex),
                                rating,
                                cursor.getString(dateIndex),
                                bookTitle,
                                bookAuthor
                        );
                        reviews.add(review);
                    }
                } while (cursor.moveToNext());
            }
            return reviews;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public boolean hasUserReviewedBook(int userId, int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_REVIEWS,
                    new String[]{COLUMN_REVIEW_ID},
                    COLUMN_USER_ID + " = ? AND " + COLUMN_BOOK_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(bookId)},
                    null, null, null);

            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // Метод для обновления существующего отзыва
    public boolean updateReview(int reviewId, String reviewText, int rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_RATING, rating);
        
        // Обновляем запись
        int rowsAffected = db.update(TABLE_REVIEWS, values,
                COLUMN_REVIEW_ID + " = ?",
                new String[]{String.valueOf(reviewId)});
        db.close();
        return rowsAffected > 0;
    }
    
    // Метод для получения отзыва по его ID
    public Review getReviewById(int reviewId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Review review = null;
        
        try {
            String query = "SELECT r.*, u." + COLUMN_USERNAME + ", b." + COLUMN_BOOK_TITLE + ", b." + COLUMN_BOOK_AUTHOR +
                    " FROM " + TABLE_REVIEWS + " r " +
                    " LEFT JOIN " + TABLE_USERS + " u ON r." + COLUMN_USER_ID + " = u." + COLUMN_USER_ID +
                    " LEFT JOIN " + TABLE_BOOKS + " b ON r." + COLUMN_BOOK_ID + " = b." + COLUMN_BOOK_ID +
                    " WHERE r." + COLUMN_REVIEW_ID + " = ?";
                    
            cursor = db.rawQuery(query, new String[]{String.valueOf(reviewId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_REVIEW_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                int bookIdIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int textIndex = cursor.getColumnIndex(COLUMN_REVIEW_TEXT);
                int ratingIndex = cursor.getColumnIndex(COLUMN_RATING);
                int dateIndex = cursor.getColumnIndex(COLUMN_REVIEW_DATE);
                int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
                
                if (idIndex >= 0 && userIdIndex >= 0 && textIndex >= 0 && dateIndex >= 0) {
                    int bookId = -1;
                    if (bookIdIndex >= 0) {
                        bookId = cursor.getInt(bookIdIndex);
                    }
                    
                    int rating = 0;
                    if (ratingIndex >= 0) {
                        rating = cursor.getInt(ratingIndex);
                    }
                    
                    String username = "";
                    if (usernameIndex >= 0) {
                        username = cursor.getString(usernameIndex);
                    }
                    
                    String bookTitle = "";
                    String bookAuthor = "";
                    if (titleIndex >= 0 && bookId > 0) {
                        bookTitle = cursor.getString(titleIndex);
                    }
                    if (authorIndex >= 0 && bookId > 0) {
                        bookAuthor = cursor.getString(authorIndex);
                    }
                    
                    if (bookId > 0 && !username.isEmpty()) {
                        review = new Review(
                            cursor.getInt(idIndex),
                            cursor.getInt(userIdIndex),
                            bookId,
                            cursor.getString(textIndex),
                            rating,
                            cursor.getString(dateIndex),
                            username
                        );
                    } else if (bookId > 0) {
                        review = new Review(
                            cursor.getInt(idIndex),
                            cursor.getInt(userIdIndex),
                            bookId,
                            cursor.getString(textIndex),
                            rating,
                            cursor.getString(dateIndex),
                            bookTitle,
                            bookAuthor
                        );
                    } else {
                        // Общий отзыв
                        review = new Review(
                            cursor.getInt(idIndex),
                            cursor.getInt(userIdIndex),
                            -1,
                            cursor.getString(textIndex),
                            0,
                            cursor.getString(dateIndex),
                            username
                        );
                    }
                }
            }
            return review;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
}