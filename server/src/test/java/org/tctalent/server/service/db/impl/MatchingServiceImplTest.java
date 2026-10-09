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

package org.tctalent.server.service.db.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.repository.db.MatchingRepository;

@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private MatchingRepository matchingRepository;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    @Test
    @DisplayName("createMatching saves a new Matching with the given description")
    void createMatching_savesNewMatching() {
        given(matchingRepository.save(any(Matching.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        Matching result = matchingService.createMatching("Nurses with ICU experience");

        ArgumentCaptor<Matching> captor = ArgumentCaptor.forClass(Matching.class);
        verify(matchingRepository).save(captor.capture());
        assertThat(result).isSameAs(captor.getValue());
        assertThat(result.getMatchingDescription()).isEqualTo("Nurses with ICU experience");
        assertThat(result.getUpdatedDate()).isNotNull();
    }

    @Test
    @DisplayName("createMatching with no description saves a Matching with a null description")
    void createMatching_nullDescription() {
        given(matchingRepository.save(any(Matching.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        Matching result = matchingService.createMatching(null);

        assertThat(result.getMatchingDescription()).isNull();
        assertThat(result.getUpdatedDate()).isNotNull();
    }

    @Test
    @DisplayName("getMatching throws NoSuchObjectException for an unknown id")
    void getMatching_unknownId_throws() {
        given(matchingRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.getMatching(99L))
            .isInstanceOf(NoSuchObjectException.class);
    }

    @Test
    @DisplayName("updateMatchingDescription saves when the description changes")
    void updateMatchingDescription_changed_saves() {
        Matching matching = new Matching("Original");
        given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));
        given(matchingRepository.save(matching)).willReturn(matching);

        Matching result = matchingService.updateMatchingDescription(1L, "Refined");

        assertThat(result.getMatchingDescription()).isEqualTo("Refined");
        verify(matchingRepository).save(matching);
    }

    @Test
    @DisplayName("updateMatchingDescription does not save when the description is unchanged")
    void updateMatchingDescription_unchanged_doesNotSave() {
        Matching matching = new Matching("Original");
        given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

        Matching result = matchingService.updateMatchingDescription(1L, "Original");

        assertThat(result).isSameAs(matching);
        verify(matchingRepository, never()).save(any(Matching.class));
    }

    @Test
    @DisplayName("updateMatchingDescription throws NoSuchObjectException for an unknown id")
    void updateMatchingDescription_unknownId_throws() {
        given(matchingRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.updateMatchingDescription(99L, "Anything"))
            .isInstanceOf(NoSuchObjectException.class);
    }
}
