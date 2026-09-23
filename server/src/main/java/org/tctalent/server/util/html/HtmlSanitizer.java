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

package org.tctalent.server.util.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * <a href="https://jsoup.org/">Jsoup</a> implementation of HTML Sanitization
 *
 * @author Tim Hill
 */
@Service
public class HtmlSanitizer {

    /**
     * Given an untrusted HTML string, remove any tags that might contribute to a cross-site
     * scripting (XSS) attack
     * Ref: <a href="https://owasp.org/www-community/attacks/xss/">OWASP website</a>
     *
     * @param html an untrusted string that could be HTML.
     * @return a string with any potential Cross Site Scripting tags removed, null if the input
     * string was null.
     */
    @Nullable
    public static String sanitize(@Nullable String html) {
        if (html == null) {
            return null;
        }

        return StringSanitizer.removeControlCharacters(
            JSoupCleanNotPretty(html, Safelist.relaxed()));
    }

    /**
     * Similar to the sanitize method above except that it is only intended for use sanitizing
     * text in Chat posts.
     * <p>
     * It does not strip <a> tags of 'target=' or 'rel=' to allow links to open in a new tab.
     * As adding this target attribute back can open up a site to risks, also adding the attribute
     * "rel=noopener" or "rel=noreferrer" helps avoid these issues.
     * See
     * <a href="https://developer.chrome.com/docs/lighthouse/best-practices/external-anchors-use-rel-noopener/">
     *     here.</a>
     * @param html an untrusted HTML string
     * @return an HTML string with any potential XSS tags removed but allowing links to open in
     * a new tab safely, or null if input text was null
     */
    @Nullable
    public static String sanitizeWithLinksNewTab(@Nullable String html) {
        return html == null ? null :
            StringSanitizer.removeControlCharacters(
                JSoupCleanNotPretty(html, Safelist.relaxed()
                                          .addAttributes("a", "target", "rel"))
            );
    }

    /**
     * Calls JSoup.clean() with prettyPrint=false.
     * Pretty print=true is the default, but it is unnecessary, and it has an undesirable side effect
     * of inserting newlines (which breaks the syntax of any encoded JSON strings).
     */
    private static String JSoupCleanNotPretty(String html, Safelist safelist) {
        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.prettyPrint(false);
        return Jsoup.clean(html, "", safelist, outputSettings);
    }

}
