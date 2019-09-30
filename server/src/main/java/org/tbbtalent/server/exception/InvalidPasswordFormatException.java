package org.tbbtalent.server.exception;

public class InvalidPasswordFormatException extends ServiceException {

    public InvalidPasswordFormatException(String message) {
        super("invalid_password_format", message);
    }
}

