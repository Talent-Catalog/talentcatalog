package org.tbbtalent.server.exception;

public class InvalidRequestException extends ServiceException {

    public InvalidRequestException(String message) {
        super("invalid_request", message);
    }
}

