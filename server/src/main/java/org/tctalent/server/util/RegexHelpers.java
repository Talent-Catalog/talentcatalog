// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Some useful Regex based utilities
 *
 * @author John Cameron
 */
public abstract class RegexHelpers {
    //Initial Regex pattern to extract urls from html links.
    private static final Pattern findUrls = Pattern.compile("<a href=\"(\\S+)\"");

    /**
     * Searches the given html string looking for links of the form <a href="...">, and extracts
     * the url(s) found in them.
     * @param html String to search
     * @return List of urls detected - not null but may be empty.
     */
    @NonNull
    public static List<String> extractLinkUrlsFromHtml(@Nullable String html) {
        //Scan content looking for links their associated urls.
        List<String> urls = new ArrayList<>();
        if (html != null) {
            Matcher matcher = findUrls.matcher(html);
            while (matcher.find()) {
                urls.add(matcher.group(1));  // Extract captured URL
            }
        }
        return urls;
    }

    public static @NonNull String camelToSnakeCase(@NonNull String camel) {
        return camel.replaceAll("([a-z,0-9])([A-Z]+)", "$1_$2")
            .toLowerCase(Locale.ENGLISH);
    }
}
