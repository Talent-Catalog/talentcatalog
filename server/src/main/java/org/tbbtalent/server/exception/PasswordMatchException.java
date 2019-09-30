package org.tbbtalent.server.exception;

public class PasswordMatchException extends ServiceException {

    public PasswordMatchException() {
        super("invalid_password_match", "The confirmation password did not match");
    }

}
