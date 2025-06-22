package com.freader.dev.db.library;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class LibraryDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "library.db";
  private static final int DATABASE_VERSION = 1;

  private static final String TABLE_BOOKS = "books";
  private static final String COLUMN_TITLE = "title";
  private static final String COLUMN_COVER = "cover";
  private static final String BOOK_PATH = "book_path";

  public LibraryDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Called when the DB is created for the first time
  @Override
  public void onCreate(SQLiteDatabase db) {
    String CREATE_BOOKS_TABLE =
        "CREATE TABLE "
            + TABLE_BOOKS
            + " ("
            + COLUMN_TITLE
            + " TEXT, "
            + COLUMN_COVER
            + " TEXT, "
            + BOOK_PATH
            + " TEXT) ";
    db.execSQL(CREATE_BOOKS_TABLE);
  }

  // Called when the DB needs to be upgraded
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
    onCreate(db);
  }

  // Add a book to the database
  public void addBook(BookModel book) {
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(COLUMN_TITLE, book.getTitle());
    values.put(COLUMN_COVER, book.getCover());
    values.put(BOOK_PATH, book.getBookPath());

    db.insert(TABLE_BOOKS, null, values);
    db.close();
  }

  // Check if the books table is not empty
  public boolean hasBooks() {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_BOOKS + " LIMIT 1", null);
    boolean hasData = cursor.moveToFirst();
    cursor.close();
    return hasData;
  }

  // Get all books from the database
  public List<BookModel> getAllBooks() {
    List<BookModel> bookList = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();

    Cursor cursor =
        db.query(
            TABLE_BOOKS,
            new String[] {COLUMN_TITLE, COLUMN_COVER, BOOK_PATH},
            null,
            null,
            null,
            null,
            null);

    if (cursor.moveToFirst()) {
      do {
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        String cover = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COVER));
        String bookPath = cursor.getString(cursor.getColumnIndexOrThrow(BOOK_PATH));

        BookModel book = new BookModel(title, cover);
        book.setBookPath(bookPath);
        bookList.add(book);
      } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();
    return bookList;
  }
}
