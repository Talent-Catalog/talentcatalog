/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.exception;

/**
 * Exception thrown when the payload for a Verify Plus scan is invalid. This could occur due to
 * reasons such as malformed data, missing required fields, or other validation errors. This exception
 * is a subclass of InvalidRequestException and is used to indicate that the request payload does not
 * meet the expected format or content requirements for processing a Verify Plus scan.
 *
 * @author sadatmalik
 */
public class InvalidVerifyPlusPayloadException extends InvalidRequestException {

    public InvalidVerifyPlusPayloadException(String message) {
        super(message);
    }

    public InvalidVerifyPlusPayloadException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
