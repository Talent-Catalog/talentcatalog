package org.tbbtalent.server.exception;

public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String type) {
        super("An account has already been created with this " + type);
    }
}

