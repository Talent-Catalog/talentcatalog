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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.util.text.TextParts;
import org.tctalent.server.util.text.TextPartsCodec;

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
     * @param htmlOrTextPartsJson an untrusted string that could be HTML or a JSON encoded
     *                            TextParts object containing HTML.
     * @return a string with any potential Cross Site Scripting tags removed, null if the input
     * string was null.
     */
    @Nullable
    public static String sanitize(@Nullable String htmlOrTextPartsJson) {
        if (htmlOrTextPartsJson == null) {
            return null;
        }

        //Check for a JSON encoded TextParts object. Try reading it as Json.
        try {
            TextParts parts = TextPartsCodec.readJson(htmlOrTextPartsJson);
            // If the HTML is a valid JSON-encoded TextParts object, return its original text.
            return TextPartsCodec.write(sanitizeTextParts(parts));
        } catch (IllegalArgumentException e) {
            // If the HTML is not a valid JSON-encoded TextParts object,
            // just sanitize it as a regular HTML string.
            return StringSanitizer.removeControlCharacters(
                JSoupCleanNotPretty(htmlOrTextPartsJson, Safelist.relaxed()));
        }
    }

    /**
     * Sanitizes the original and tidied text in the given TextParts object.
     * @param textParts a TextParts object containing original and tidied text to be sanitized.
     * @return a new TextParts object with the original and tidied text sanitized,
     * but with the same keywords as the input.
     */
    @NonNull
    private static TextParts sanitizeTextParts(@NonNull TextParts textParts) {
        TextParts sanitized = new TextParts();
        sanitized.setOriginal(sanitize(textParts.getOriginal()));
        sanitized.setTidied(sanitize(textParts.getTidied()));
        sanitized.setKeywords(textParts.getKeywords());
        return sanitized;
    }

    /**
     * Similar to the sanitize method above except that it does not process TextParts json.
     * It is only intended for use sanitizing text in Chat posts.
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
