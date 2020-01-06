package org.tbbtalent.server.exception;

public class EntityReferencedException extends ServiceException {

    public EntityReferencedException(String type) {
        super("entity_referenced", "This " + type+ " is referenced by another object and cannot be deleted");
    }
}

