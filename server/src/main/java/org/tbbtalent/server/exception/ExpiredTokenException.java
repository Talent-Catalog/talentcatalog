package org.tbbtalent.server.exception;

public class ExpiredTokenException extends ServiceException {

    public ExpiredTokenException() {
        super("expired_password_token", "The reset password token has expired");
    }

    public ExpiredTokenException(Throwable cause) {
        super("expired_password_token", "The reset password token has expired", cause);
    }
}
