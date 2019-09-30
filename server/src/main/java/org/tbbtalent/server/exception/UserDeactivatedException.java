package org.tbbtalent.server.exception;

public class UserDeactivatedException extends RuntimeException {

    public UserDeactivatedException() {
        super("This account has been deactivated");
    }

}
