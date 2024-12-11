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

package org.tctalent.server.service.db;

import org.tctalent.server.exception.ReCaptchaInvalidException;

/**
 * Service for managing Google reCaptcha validation.
 * Used to prevent credential stuffing attacks.
 * Automated data entry - eg for registration will be ignored.
 * <p>
 *     Based on https://www.baeldung.com/spring-security-registration-captcha
 * </p>
 * <p>
 *     reCaptcha - see https://www.google.com/recaptcha/admin/
 *     Talent Catalog is currently managed out of Google account john@cameronfoundation.org,
 *     with jcameron@talentbeyondboundaries.org as a notifiable email.
 *     The one reCaptcha can serve multiple domains associated with the same site - eg
 *     tctalent.org, unhcrtalent.org, iomtalent.org, localhost, 127.0.0.1 etc.
 *     Note that subdomains do not need extra configuration - you just need to configure the
 *     domain. So the fact that displacedtalent.org is currently configured in reCaptcha means that
 *     any subdomain of that doesn't require any extra configuration - eg tbb.displacedtalent.org,
 *     unhcr.displacedtalent.org etc
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
