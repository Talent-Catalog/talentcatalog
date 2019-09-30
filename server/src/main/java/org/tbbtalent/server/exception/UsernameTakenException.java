package org.tbbtalent.server.exception;

public class UsernameTakenException extends ServiceException {

    public UsernameTakenException(String type) {
        super("username_taken", "An account has already been created with this " + type);
    }
}

