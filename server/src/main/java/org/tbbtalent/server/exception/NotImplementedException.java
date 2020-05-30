/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.exception;

public class NotImplementedException extends ServiceException {
    public NotImplementedException(Class objectClass, String method) {
        this(objectClass.getSimpleName(), method);
    }
    public NotImplementedException(String className, String method) {
        super("not_implemented", 
                "Method '" + method + "' of " + className 
                        + " is not implemented.");
    }
}
