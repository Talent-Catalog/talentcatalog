package org.tbbtalent.server.exception;

public class NoSuchObjectException extends RuntimeException {

    public NoSuchObjectException(Class objectClass, long id) {
        super("Missing " + objectClass.getSimpleName() + " with ID " + id);
    }
}

