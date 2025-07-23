package com.company.MultiModule.exceptions;

public class BorrowLimitExceed extends LibraryException {
    public BorrowLimitExceed(String userName, int limit) {
        super("User '" + userName + "' has exceeded the borrow limit of " + limit + " books.");
    }
}