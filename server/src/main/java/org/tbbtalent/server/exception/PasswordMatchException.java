package org.tbbtalent.server.exception;

public class PasswordMatchException extends RuntimeException {

    public PasswordMatchException() {
        super("The confirmation password did not match");
    }

}
