package org.tbbtalent.server.exception;

public class EmailSendFailedException extends ServiceException {

    public EmailSendFailedException() {
        super("email_send_failed", "The email could not be sent");
    }

    public EmailSendFailedException(Throwable cause) {
        super("email_send_failed", "The email could not be sent", cause);
    }
}
