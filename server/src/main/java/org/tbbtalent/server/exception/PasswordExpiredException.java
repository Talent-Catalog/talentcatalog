package org.tbbtalent.server.exception;

public class PasswordExpiredException extends ServiceException {

    public PasswordExpiredException() {
        super("password_expired", "Password has expired for this account");
    }

}
