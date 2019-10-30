package org.tbbtalent.server.exception;

public class NoSuchObjectException extends ServiceException {

    public NoSuchObjectException(Class objectClass, long id) {
        super("missing_object", "Missing " + objectClass.getSimpleName() + " with ID " + id);
    }

    public NoSuchObjectException(Class objectClass, String email) {
        super("missing_object", "Missing " + objectClass.getSimpleName() + " with email " + email);
    }
}

