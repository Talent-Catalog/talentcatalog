/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.util;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RegexHelpersTest {
    RegexHelpers regexHelpers = new RegexHelpers();

    @Test
    void extractLinkUrlsFromHtml() {
        List<String> urls;

        urls = regexHelpers.extractLinkUrlsFromHtml(null);
        Assertions.assertNotNull(urls);
        Assertions.assertEquals(0, urls.size());

        urls = regexHelpers.extractLinkUrlsFromHtml("Other stuff, blah blah <a>Blah blah</a>");
        Assertions.assertNotNull(urls);
        Assertions.assertEquals(0, urls.size());

        urls = regexHelpers.extractLinkUrlsFromHtml(
            "Other stuff, blah blah <a href=\"http://example.com\">http://example.com</a>"
                + "more blah <a href=\"http://example2.com\">Anything</a>");
        Assertions.assertNotNull(urls);
        Assertions.assertEquals(2, urls.size());
        Assertions.assertEquals("http://example.com", urls.get(0));
        Assertions.assertEquals("http://example2.com", urls.get(1));
    }
}
