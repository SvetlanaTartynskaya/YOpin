package com.example.yopin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books;
    private OnBookClickListener listener;
    private Context context;
    private int userId;

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
        holder.tvBookGenre.setText("Жанр: " + book.getGenre());
        holder.tvBookYear.setText("Год: " + book.getYear());

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
                    
                    if (dbHelper.hasUserReviewedBook(userId, bookId)) {
                        // Пользователь уже оставил отзыв, перенаправляем на страницу книги
                        Intent intent = new Intent(context, BookReviewsActivity.class);
                        intent.putExtra("book_id", bookId);
                        intent.putExtra("email", ((BooksActivity)context).getUserEmail());
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
        TextView tvBookTitle, tvBookAuthor, tvBookGenre, tvBookYear;
        Button btnWriteReview;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookGenre = itemView.findViewById(R.id.tvBookGenre);
            tvBookYear = itemView.findViewById(R.id.tvBookYear);
            btnWriteReview = itemView.findViewById(R.id.btnWriteReview);
        }
    }
} 