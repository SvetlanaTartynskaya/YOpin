package com.example.yopin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BookReviews.db";
    private static final int DATABASE_VERSION = 1;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";

    // Таблица отзывов
    private static final String TABLE_REVIEWS = "reviews";
    private static final String COLUMN_REVIEW_ID = "review_id";
    private static final String COLUMN_BOOK_TITLE = "book_title";
    private static final String COLUMN_REVIEW_TEXT = "review_text";
    private static final String COLUMN_REVIEW_USER_ID = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_EMAIL + " TEXT,"
                + COLUMN_USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Создание таблицы отзывов
        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + "("
                + COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BOOK_TITLE + " TEXT,"
                + COLUMN_REVIEW_TEXT + " TEXT,"
                + COLUMN_REVIEW_USER_ID + " INTEGER" + ")";
        db.execSQL(CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }

    // Метод для добавления пользователя
    public long addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    // Метод для проверки пользователя
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Метод для добавления отзыва
    public long addReview(String bookTitle, String reviewText, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_TITLE, bookTitle);
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_REVIEW_USER_ID, userId);
        return db.insert(TABLE_REVIEWS, null, values);
    }

    // Метод для получения всех отзывов
    public Cursor getAllReviews() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_REVIEWS, null, null, null, null, null, null);
    }
}