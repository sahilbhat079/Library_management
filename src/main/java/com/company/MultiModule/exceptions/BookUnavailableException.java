package com.company.MultiModule.exceptions;

public class BookUnavailableException extends LibraryException {
    public BookUnavailableException(String bookTitle, long timeoutSeconds) {
        super("Book '" + bookTitle + "' is not available after waiting " + timeoutSeconds + " seconds.");
    }
}