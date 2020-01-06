package org.tbbtalent.server.exception;

public class EntityExistsException extends RuntimeException {

    public EntityExistsException(String type) {
        super("A " + type + " with this name already exists");
    }
}

