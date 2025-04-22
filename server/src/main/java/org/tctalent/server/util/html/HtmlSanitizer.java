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

package org.tctalent.server.util.html;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.lang.Nullable;
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
     * @return an HTML string with any potentially Cross Site Scripting tags removed, null if html
     * was null.
     */
    @Nullable
    public static String sanitize(@Nullable String html) {
        return html == null ? null :
            StringSanitizer.replaceControlCharacters(Jsoup.clean(html, Safelist.relaxed()));
    }

    /**
     * Same as sanitize method above, but without stripping <a> tags of 'target=' or 'rel=' to allow links to open in new tab.
     * As adding this target attribute back can open up site to risks, also adding the attribute "rel=noopener" or
     * "rel=noreferrer" helps avoid these issues.
     * See here: https://developer.chrome.com/docs/lighthouse/best-practices/external-anchors-use-rel-noopener/
     * @param html an untrusted HTML string
     * @return an HTML string with any potentially XSS tags removed but allowing links to open in new tab safely, or
     * null if html was null
     */
    @Nullable
    public static String sanitizeWithLinksNewTab(@Nullable String html) {
        return html == null ? null :
            StringSanitizer.replaceControlCharacters(
                Jsoup.clean(html, Safelist.relaxed().addAttributes("a", "target", "rel"))
            );
    }

}
