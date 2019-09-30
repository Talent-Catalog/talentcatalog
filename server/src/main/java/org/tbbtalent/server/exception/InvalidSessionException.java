package org.tbbtalent.server.exception;

public class InvalidSessionException extends ServiceException {

    public InvalidSessionException(String message) {
        super("invalid_session", message);
    }
}

