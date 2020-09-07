package org.tbbtalent.server.exception;

public class EntityExistsException extends ServiceException {

    public EntityExistsException(String type) {
        super("entity_exists", "A " + type + " with this name already exists");
    }
}

