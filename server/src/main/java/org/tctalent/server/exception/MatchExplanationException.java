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
 * Thrown when an on-demand candidate/opportunity match explanation cannot be generated - for
 * example because the Python match explanation service returned no result, returned a result for
 * the wrong candidate, reported an item-level failure, or could not be reached.
 */
public class MatchExplanationException extends ServiceException {

    public MatchExplanationException(String message) {
        super("match_explanation_error", message);
    }

    public MatchExplanationException(String message, Throwable cause) {
        super("match_explanation_error", message, cause);
    }
}
