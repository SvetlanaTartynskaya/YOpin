package com.example.yopin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {
    private List<Book> books;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(int bookId);
    }

    public BooksAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book, listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvAuthor;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
        }

        public void bind(final Book book, final OnBookClickListener listener) {
            tvTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());

            itemView.setOnClickListener(v -> listener.onBookClick(book.getId()));
        }
    }
}