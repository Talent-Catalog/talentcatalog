/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.exception;

/**
 * Thrown when reCaptcha checks fail
 *
 * @author John Cameron
 */
public final class ReCaptchaInvalidException extends ServiceException {

    public ReCaptchaInvalidException(final String message) {
        this(message, null);
    }

    public ReCaptchaInvalidException(String message,
                                 Throwable cause) {
        super("recaptcha", message, cause);
    }

}