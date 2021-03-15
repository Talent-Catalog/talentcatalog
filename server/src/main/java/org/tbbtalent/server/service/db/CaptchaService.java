/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db;

import org.tbbtalent.server.exception.ReCaptchaInvalidException;

/**
 * Service for managing Google reCaptcha validation.
 * Used to prevent credential stuffing attacks. 
 * Automated data entry - eg for registration will be ignored.
 * <p>
 *     Based on https://www.baeldung.com/spring-security-registration-captcha
 * </p>
 *
 * @author John Cameron
 */
public interface CaptchaService {
    void processCaptchaV3Token(final String token, String action) 
            throws ReCaptchaInvalidException;

    String getReCaptchaSite();

    String getReCaptchaSecret();
}
