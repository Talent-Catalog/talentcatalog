/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.exception;

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
