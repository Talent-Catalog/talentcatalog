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

package org.tctalent.server.repository.db;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;
import org.tctalent.server.model.db.Matching;

class MatchingRepositoryIntegrationTest extends BaseJpaIntegrationTest {

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("persists a Matching with a database-generated id, and reloads its description")
    void saveAndReload() {
        Matching saved = matchingRepository.saveAndFlush(new Matching("Nurses with ICU experience"));
        assertThat(saved.getId()).isNotNull();
        entityManager.clear();

        Matching reloaded = matchingRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getMatchingDescription()).isEqualTo("Nurses with ICU experience");
    }

    @Test
    @DisplayName("persists a Matching with no description")
    void saveAndReload_nullDescription() {
        Matching saved = matchingRepository.saveAndFlush(new Matching(null));
        entityManager.clear();

        Matching reloaded = matchingRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getMatchingDescription()).isNull();
        assertThat(reloaded.getUpdatedDate()).isNotNull();
    }

    @Test
    @DisplayName("reloaded updatedDate is exactly equal to the in-memory value it was saved with")
    void updatedDate_roundTripsExactly() {
        Matching matching = new Matching("Original");
        matching = matchingRepository.saveAndFlush(matching);
        matching.updateMatchingDescription("Refined");
        matching = matchingRepository.saveAndFlush(matching);
        //This is the kind of snapshot a match explanation will take of the Matching.
        OffsetDateTime snapshot = matching.getUpdatedDate();
        entityManager.clear();

        Matching reloaded = matchingRepository.findById(matching.getId()).orElseThrow();

        //Plain equals (not isEqual) - the offset as well as the instant must survive.
        assertThat(reloaded.getUpdatedDate()).isEqualTo(snapshot);
        assertThat(reloaded.getMatchingDescription()).isEqualTo("Refined");
    }
}
