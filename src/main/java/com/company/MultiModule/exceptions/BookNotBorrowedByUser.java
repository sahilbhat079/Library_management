package com.company.MultiModule.exceptions;

public class BookNotBorrowedByUser extends LibraryException {

    public BookNotBorrowedByUser(String userId, String bookId) {
        super("User with ID '" + userId + "' did not borrow book with ID '" + bookId + "'.");
    }
}