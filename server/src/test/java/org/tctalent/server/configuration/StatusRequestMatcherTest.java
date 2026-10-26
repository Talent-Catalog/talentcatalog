/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Regression test for the unusual "/status**" pattern which SecurityConfiguration permits.
 * <p>
 * Spring Security 7 always matches requestMatchers(String) patterns with a
 * PathPatternRequestMatcher, which uses Spring MVC's PathPattern syntax, as Spring MVC already
 * did under Spring Security 6 - TC-1189.
 * </p>
 */
class StatusRequestMatcherTest {

    //The same patterns as in SecurityConfiguration
    private final RequestMatcher statusMatcher = new OrRequestMatcher(
        PathPatternRequestMatcher.withDefaults().matcher("/status**"),
        PathPatternRequestMatcher.withDefaults().matcher("/status/**"));

    @ParameterizedTest
    @CsvSource({
        "/status, true",
        "/status/, true",
        "/status/abc, true",
        "/status/abc/def, true",
        //"**" within a path segment matches any characters in that segment
        "/statusabc, true",
        "/statuses, true",
        "/api/status, false",
        "/stat, false",
        "/api/admin/candidate/status, false"
    })
    void statusPatterns(String path, boolean expectedMatch) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        assertEquals(expectedMatch, statusMatcher.matches(request), path);
    }
}
