package org.tbbtalent.server.exception;

public class CircularReferencedException extends ServiceException {

    public CircularReferencedException(Long id) {
        super("circular_reference", "Search with "+ id+ " is part of a circular reference");
    }
}

