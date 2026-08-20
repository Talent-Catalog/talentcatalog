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

package org.tctalent.server.request.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Mirrors the equivalent frontend tests in
 * ui/candidate-portal/src/app/model/base.spec.ts (sanitizeEmailInput) - see TC-723.
 */
class BaseCandidateContactRequestTest {

    private String sanitize(String email) {
        BaseCandidateContactRequest request = new BaseCandidateContactRequest();
        request.setEmail(email);
        return request.getEmail();
    }

    @Test
    void returnsACleanEmailAddressUnchanged() {
        assertEquals("example@example.com", sanitize("example@example.com"));
    }

    @Test
    void stripsAHangulFillerCharacterPastedBeforeTheAddress() {
        assertEquals("example@example.com", sanitize("ㅤexample@example.com"));
    }

    @Test
    void stripsAZeroWidthSpacePastedAfterTheAddress() {
        assertEquals("example@example.com", sanitize("example@example.com​"));
    }

    @Test
    void leavesAZeroWidthSpaceEmbeddedInTheMiddleOfTheAddressUntouched() {
        assertEquals("example@example​.com", sanitize("example@example​.com"));
    }

    @Test
    void leavesAnOrdinarySpaceEmbeddedInTheMiddleOfTheAddressUntouched() {
        assertEquals("exam ple@example.com", sanitize("exam ple@example.com"));
    }

    @Test
    void trimsLeadingAndTrailingWhitespace() {
        assertEquals("example@example.com", sanitize("  example@example.com  "));
    }

    @Test
    void returnsAnEmptyStringUnchanged() {
        assertEquals("", sanitize(""));
    }

    @Test
    void returnsNullUnchanged() {
        assertNull(sanitize(null));
    }
}
