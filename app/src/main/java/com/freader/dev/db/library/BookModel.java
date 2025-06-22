package com.freader.dev.db.library;

import java.util.Objects;

public class BookModel {

  private String title;
  private String cover;
  private String bookPath;

  public BookModel(String title, String cover) {
    this.title = title;
    this.cover = cover;
  }

  public String getTitle() {
    return title;
  }

  public String getCover() {
    return cover;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BookModel book = (BookModel) o;
    return Objects.equals(title, book.title) && Objects.equals(cover, book.cover);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, cover);
  }

  // Setters
  public void setBookPath(String path) {
    this.bookPath = path;
  }

  // Getters
  public String getBookPath() {
    return bookPath;
  }
}
