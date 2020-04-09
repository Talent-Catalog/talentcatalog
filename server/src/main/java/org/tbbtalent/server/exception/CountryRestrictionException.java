package org.tbbtalent.server.exception;

public class CountryRestrictionException extends ServiceException {

    public CountryRestrictionException(String message) {
        super("invalid_request", message);
    }
}

