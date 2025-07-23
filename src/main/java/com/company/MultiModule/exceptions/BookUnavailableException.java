package com.company.MultiModule.exceptions;

public class BookUnavailableException extends LibraryException {
    public BookUnavailableException(String bookTitle) {
        super("Book '" + bookTitle + "' is currently not available. Please try again later.");
    }
}
