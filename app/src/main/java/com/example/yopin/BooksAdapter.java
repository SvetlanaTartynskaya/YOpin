package com.example.yopin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.ViewHolder> {
    private List<Book> books;
    private Context context;

    public BooksAdapter(List<Book> books, Context context) {
        this.books = books;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        
        holder.tvBookTitle.setText(book.getTitle());
        holder.tvBookAuthor.setText(book.getAuthor());
        
        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            holder.tvBookGenre.setText("Жанр: " + book.getGenre());
            holder.tvBookGenre.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookGenre.setVisibility(View.GONE);
        }
        
        if (book.getYear() != null && !book.getYear().isEmpty()) {
            holder.tvBookYear.setText("Год: " + book.getYear());
            holder.tvBookYear.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookYear.setVisibility(View.GONE);
        }
        
        // Настройка нажатия на карточку книги
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookReviewsActivity.class);
            intent.putExtra("book_id", book.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvBookAuthor, tvBookGenre, tvBookYear;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookGenre = itemView.findViewById(R.id.tvBookGenre);
            tvBookYear = itemView.findViewById(R.id.tvBookYear);
        }
    }
}