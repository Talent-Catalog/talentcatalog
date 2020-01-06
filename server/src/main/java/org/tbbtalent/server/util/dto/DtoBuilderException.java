package org.tbbtalent.server.util.dto;

public class DtoBuilderException extends RuntimeException {

    public DtoBuilderException(String message) {
        super(message);
    }

    public DtoBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
