package com.company.MultiModule.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Book {
    private final String id;
    private final String title;
    private final String author;
    private final String category;
    private final String isbn;
    private boolean available;

    protected Book(Builder<?> builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.title = builder.title;
        this.author = builder.author;
        this.category = builder.category;
        this.isbn = builder.isbn;
        this.available = builder.available;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format(
                "Book{id='%s', title='%s', author='%s', category='%s', isbn='%s', available=%s}",
                id, title, author, category, isbn, available
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Builder<T extends Builder<T>> {
        private String id;
        private String title;
        private String author;
        private String category;
        private String isbn;
        private boolean available = true;

        public T id(String id) {
            this.id = id;
            return self();
        }

        public T title(String title) {
            this.title = title;
            return self();
        }

        public T author(String author) {
            this.author = author;
            return self();
        }

        public T category(String category) {
            this.category = category;
            return self();
        }

        public T isbn(String isbn) {
            this.isbn = isbn;
            return self();
        }

        public T available(boolean available) {
            this.available = available;
            return self();
        }

        protected T self() {
            return (T) this;
        }

        public Book build() {
            return new Book(this);
        }
    }
}
