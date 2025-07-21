package com.company.MultiModule.exceptions;

public class UserNotFound extends LibraryException {
    public UserNotFound(String userId) {
        super("User with ID '" + userId + "' not found.");
    }
}
