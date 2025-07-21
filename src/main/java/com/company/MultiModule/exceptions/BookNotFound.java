package com.company.MultiModule.exceptions;

public class BookNotFound extends LibraryException {
    public BookNotFound(String bookId) {
        super("Book with ID '" + bookId + "' not found.");
    }
}