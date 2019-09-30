package org.tbbtalent.server.exception;

public class UserDeactivatedException extends ServiceException {

    public UserDeactivatedException() {
        super("user_deactivated", "This account has been deactivated");
    }

}
