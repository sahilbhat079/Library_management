package com.company.MultiModule.exceptions;

public class BookAlreadyBorrowed extends LibraryException {
    public BookAlreadyBorrowed(String bookId) {
        super("Book with ID '" + bookId + "' is already borrowed.");
    }
}