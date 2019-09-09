package org.tbbtalent.server.exception;

public class InvalidPasswordFormatException extends RuntimeException {

    public InvalidPasswordFormatException(String message) {
        super(message);
    }
}

