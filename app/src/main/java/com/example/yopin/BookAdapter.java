package com.example.yopin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.DecimalFormat;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books;
    private OnBookClickListener listener;
    private Context context;
    private int userId;
    private DecimalFormat ratingFormat = new DecimalFormat("0.0");

    public interface OnBookClickListener {
        void onBookClick(int bookId);
    }

    public BookAdapter(List<Book> books, OnBookClickListener listener, Context context, int userId) {
        this.books = books;
        this.listener = listener;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.tvBookTitle.setText(book.getTitle());
        holder.tvBookAuthor.setText(book.getAuthor());
        
        // Жанр и год с учетом пустых значений
        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            holder.tvBookGenre.setText(book.getGenre());
            holder.tvBookGenre.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookGenre.setVisibility(View.GONE);
        }
        
        if (book.getYear() != null && !book.getYear().isEmpty()) {
            holder.tvBookYear.setText(book.getYear());
            holder.tvBookYear.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookYear.setVisibility(View.GONE);
        }
        
        // Отображение рейтинга книги
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        float rating = dbHelper.getBookAverageRating(book.getId());
        int reviewCount = dbHelper.getBookReviewCount(book.getId());
        
        holder.ratingBar.setRating(rating);
        holder.tvRatingValue.setText(ratingFormat.format(rating));
        
        // Отображение количества отзывов
        String reviewText = context.getResources().getQuantityString(
            R.plurals.review_count, reviewCount, reviewCount);
        holder.tvReviewCount.setText(reviewText);
        
        // Анимация иконки книги
        holder.ivBookIcon.setAlpha(0.9f);
        holder.ivBookIcon.animate()
            .alpha(1.0f)
            .setDuration(1000)
            .start();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBookClick(books.get(position).getId());
                }
            }
        });
        
        // Кнопка "Написать отзыв"
        holder.btnWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Проверяем, не писал ли пользователь уже отзыв на эту книгу
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    int bookId = books.get(position).getId();
                    SessionManager sessionManager = new SessionManager(context);
                    
                    if (dbHelper.hasUserReviewedBook(userId, bookId)) {
                        // Пользователь уже оставил отзыв, перенаправляем на страницу книги
                        Intent intent = new Intent(context, BookReviewsActivity.class);
                        intent.putExtra("book_id", bookId);
                        // Получаем email из SessionManager
                        intent.putExtra("email", sessionManager.getUserEmail()); 
                        context.startActivity(intent);
                    } else {
                        // Переходим на экран добавления отзыва
                        Intent intent = new Intent(context, AddReviewActivity.class);
                        intent.putExtra("book_id", bookId);
                        intent.putExtra("user_id", userId);
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvBookAuthor, tvBookGenre, tvBookYear, tvRatingValue, tvReviewCount;
        RatingBar ratingBar;
        Button btnWriteReview;
        ImageView ivBookIcon;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookGenre = itemView.findViewById(R.id.tvBookGenre);
            tvBookYear = itemView.findViewById(R.id.tvBookYear);
            btnWriteReview = itemView.findViewById(R.id.btnWriteReview);
            ratingBar = itemView.findViewById(R.id.ratingBarBook);
            tvRatingValue = itemView.findViewById(R.id.tvRatingValue);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            ivBookIcon = itemView.findViewById(R.id.ivBookIcon);
        }
    }
} 