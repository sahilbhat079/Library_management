package com.company.MultiModule.exceptions;

public class BorrowLimitExceed extends LibraryException {
    public BorrowLimitExceed(String userId, int limit) {
        super("User '" + userId + "' has exceeded the borrow limit of " + limit + " books.");
    }
}