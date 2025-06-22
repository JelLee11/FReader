package com.freader.dev.callbacks;

import com.freader.dev.db.library.BookModel;

public interface OnLibraryClickListener {
  void onBookClick(BookModel book);
}
