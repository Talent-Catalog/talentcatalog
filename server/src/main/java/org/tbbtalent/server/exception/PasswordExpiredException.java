package org.tbbtalent.server.exception;

public class PasswordExpiredException extends RuntimeException {

    public PasswordExpiredException() {
        super("Password has expired for this account");
    }

}
