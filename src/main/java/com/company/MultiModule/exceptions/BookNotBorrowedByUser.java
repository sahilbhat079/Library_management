package com.company.MultiModule.exceptions;

public class BookNotBorrowedByUser extends LibraryException {

    public BookNotBorrowedByUser(String userName, String isbn) {
        super("User with Name '" + userName + "' did not borrow book with ISBN '" + isbn + "'.");
    }
}