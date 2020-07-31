/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
