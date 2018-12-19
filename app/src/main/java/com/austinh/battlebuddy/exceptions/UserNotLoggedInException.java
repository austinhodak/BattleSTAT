package com.austinh.battlebuddy.exceptions;

public class UserNotLoggedInException extends Exception {

    public UserNotLoggedInException() {
        super();
    }

    public UserNotLoggedInException(final String message) {
        super(message);
    }
}
