package com.freader.dev.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import com.bumptech.glide.Glide;
import com.freader.dev.R;
import com.freader.dev.callbacks.OnLibraryClickListener;
import com.freader.dev.db.library.BookModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryBookAdapter extends RecyclerView.Adapter<LibraryBookAdapter.ViewHolder> {

  private Context context;
  private List<BookModel> bookModel;
  private OnLibraryClickListener clickListener;

  public LibraryBookAdapter(
      Context context, List<BookModel> bookModel, OnLibraryClickListener clickListener) {
    this.context = context;
    this.bookModel = new ArrayList<>(bookModel);
    this.clickListener = clickListener;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView image;

    public ViewHolder(View view) {
      super(view);
      image = view.findViewById(R.id.coverImage);
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.book_items, parent, false);
    RecyclerView.LayoutParams lp =
        new RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    view.setLayoutParams(lp);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    BookModel book = bookModel.get(position);

    if (book.getCover() != null) {
      TypedValue typedValue = new TypedValue();
      Resources.Theme theme = context.getTheme();
      theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true);
      int color = typedValue.data;

      CircularProgressDrawable load = new CircularProgressDrawable(context);
      load.setStrokeWidth(8f);
      load.setCenterRadius(30f);
      load.setColorSchemeColors(color);
      load.start();

      Glide.with(context)
          .load(new File(book.getCover()))
          .placeholder(load)
          .error(R.drawable.no_image)
          .into(holder.image);
    }

    // Click Listeners
    holder.itemView.setOnClickListener(
        v -> {
          v.animate()
              .alpha(0f)
              .setDuration(100)
              .withEndAction(
                  () -> {
                    v.animate().alpha(1f).setDuration(100).start();
                    new Handler(Looper.getMainLooper())
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                  clickListener.onBookClick(book);
                              }
                            },
                            500);
                  })
              .start();
        });
  }

  @Override
  public int getItemCount() {
    return bookModel.size();
  }

  // ✅ Replaces addBooks with DiffUtil-based update
  public void setBooks(List<BookModel> newBooks) {
    DiffUtil.DiffResult diffResult =
        DiffUtil.calculateDiff(new BookDiffCallback(bookModel, newBooks));
    bookModel.addAll(newBooks);
    diffResult.dispatchUpdatesTo(this);
  }

  // ✅ DiffUtil Callback
  public static class BookDiffCallback extends DiffUtil.Callback {
    private final List<BookModel> oldList;
    private final List<BookModel> newList;

    public BookDiffCallback(List<BookModel> oldList, List<BookModel> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      BookModel oldBook = oldList.get(oldItemPosition);
      BookModel newBook = newList.get(newItemPosition);
      return oldBook.getTitle().equals(newBook.getTitle())
          && oldBook.getCover().equals(newBook.getCover());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
  }
}
