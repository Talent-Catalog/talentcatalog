package org.tbbtalent.server.exception;

public class InvalidPasswordTokenException extends ServiceException {

    public InvalidPasswordTokenException() {
        super("invalid_password_token", "The reset password token is not valid");
    }

    public InvalidPasswordTokenException(Throwable cause) {
        super("invalid_password_token", "The reset password token is not valid", cause);
    }
}
