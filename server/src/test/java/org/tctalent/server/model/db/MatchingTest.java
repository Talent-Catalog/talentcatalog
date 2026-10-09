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

package org.tctalent.server.model.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class MatchingTest {

    private static final OffsetDateTime EARLIER =
        OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    @DisplayName("new Matching has the given description and a UTC microsecond-precision updatedDate")
    void newMatching_setsDescriptionAndUpdatedDate() {
        Matching matching = new Matching("Nurses with ICU experience");

        assertThat(matching.getMatchingDescription()).isEqualTo("Nurses with ICU experience");
        assertMicrosecondUtc(matching.getUpdatedDate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\n\t "})
    @DisplayName("new Matching with null or blank description has no description")
    void newMatching_blankDescriptionIsNull(String description) {
        Matching matching = new Matching(description);

        assertThat(matching.getMatchingDescription()).isNull();
        assertMicrosecondUtc(matching.getUpdatedDate());
    }

    @Test
    @DisplayName("updateMatchingDescription changes description and updatedDate when the description changes")
    void updateMatchingDescription_changed_updatesDescriptionAndDate() {
        Matching matching = matchingUpdatedEarlier("Original");

        boolean changed = matching.updateMatchingDescription("Refined");

        assertThat(changed).isTrue();
        assertThat(matching.getMatchingDescription()).isEqualTo("Refined");
        assertThat(matching.getUpdatedDate()).isAfter(EARLIER);
        assertMicrosecondUtc(matching.getUpdatedDate());
    }

    @Test
    @DisplayName("updateMatchingDescription with the same description changes nothing")
    void updateMatchingDescription_same_changesNothing() {
        Matching matching = matchingUpdatedEarlier("Original");

        boolean changed = matching.updateMatchingDescription("Original");

        assertThat(changed).isFalse();
        assertThat(matching.getMatchingDescription()).isEqualTo("Original");
        assertThat(matching.getUpdatedDate()).isEqualTo(EARLIER);
    }

    @Test
    @DisplayName("updateMatchingDescription clearing the description is a change")
    void updateMatchingDescription_cleared_isChange() {
        Matching matching = matchingUpdatedEarlier("Original");

        boolean changed = matching.updateMatchingDescription("  ");

        assertThat(changed).isTrue();
        assertThat(matching.getMatchingDescription()).isNull();
        assertThat(matching.getUpdatedDate()).isAfter(EARLIER);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\n\t "})
    @DisplayName("updateMatchingDescription with a blank description when there is none changes nothing")
    void updateMatchingDescription_blankToBlank_changesNothing(String description) {
        Matching matching = matchingUpdatedEarlier(null);

        boolean changed = matching.updateMatchingDescription(description);

        assertThat(changed).isFalse();
        assertThat(matching.getMatchingDescription()).isNull();
        assertThat(matching.getUpdatedDate()).isEqualTo(EARLIER);
    }

    @Test
    @DisplayName("updateMatchingDescription treats non-blank text differing only in whitespace as a change")
    void updateMatchingDescription_whitespaceDifference_isChange() {
        Matching matching = matchingUpdatedEarlier("Original");

        boolean changed = matching.updateMatchingDescription("Original ");

        assertThat(changed).isTrue();
        assertThat(matching.getMatchingDescription()).isEqualTo("Original ");
    }

    /**
     * Creates a Matching whose updatedDate is in the past, so that tests can reliably detect
     * whether an update changed it.
     */
    private static Matching matchingUpdatedEarlier(String description) {
        Matching matching = new Matching(description);
        ReflectionTestUtils.setField(matching, "updatedDate", EARLIER);
        return matching;
    }

    private static void assertMicrosecondUtc(OffsetDateTime dateTime) {
        assertThat(dateTime).isNotNull();
        assertThat(dateTime.getOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(dateTime.getNano() % 1000).isZero();
    }
}
