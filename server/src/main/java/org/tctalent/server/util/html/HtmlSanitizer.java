/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.util.html;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

/**
 * JSoup https://jsoup.org/ implementation of HTML Sanitization
 *
 * @author Tim Hill
 */
@Service
public class HtmlSanitizer {
    /**
     * Given an untrusted HTML string, remove any tags that might contribute to a cross-site scripting (XSS) attack
     * Ref: https://owasp.org/www-community/attacks/xss/
     *
     * @param html an untrusted HTML string
     * @return an HTML string with any potentially Cross Site Scripting tags removed
     */
    public static String sanitize(String html) {
        return Jsoup.clean(html, Safelist.relaxed());
    }
}
