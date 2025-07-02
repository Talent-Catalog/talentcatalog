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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateTestData.getCandidate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.model.db.DependantRelations;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.repository.db.CandidateDependantRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.dependant.CreateCandidateDependantRequest;

@ExtendWith(MockitoExtension.class)
class CandidateDependantServiceImplTest {

    private Candidate candidate;
    private CreateCandidateDependantRequest createRequest;
    private CandidateDependant dependant;
    private List<CandidateDependant> dependantList;
    private CandidateIntakeDataUpdate intakeData;

    private static final long CANDIDATE_ID = getCandidate().getId();
    private static final long DEPENDANT_ID = 33L;

    @Mock private CandidateDependantRepository candidateDependantRepository;
    @Mock private CandidateRepository candidateRepository;

    @Captor private ArgumentCaptor<CandidateDependant> dependantCaptor;

    @InjectMocks
    CandidateDependantServiceImpl candidateDependantService;

    @BeforeEach
    void setUp() {
        candidate = getCandidate();
        createRequest = new CreateCandidateDependantRequest();
        dependant = new CandidateDependant();
        dependant.setId(DEPENDANT_ID);
        dependant.setCandidate(candidate);
        dependantList = List.of(dependant, dependant);
        intakeData = new CandidateIntakeDataUpdate();
        intakeData.setDependantId(dependant.getId());
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void createDependant_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDependantService.createDependant(CANDIDATE_ID, createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(CANDIDATE_ID)));
    }

    @Test
    @DisplayName("should create dependant as expected")
    void createCitizenship_shouldCreateDependant() {
        createRequest.setRelation(DependantRelations.Child);
        final LocalDate dob = LocalDate.parse("2019-01-01");
        createRequest.setDob(dob);
        createRequest.setHealthConcern(YesNo.No);
        final String healthNotes = "health notes";
        createRequest.setHealthNotes(healthNotes);

        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));

        candidateDependantService.createDependant(CANDIDATE_ID, createRequest);

        verify(candidateDependantRepository).save(dependantCaptor.capture());
        CandidateDependant result = dependantCaptor.getValue();
        assertEquals(result.getCandidate(), candidate);
        assertEquals(result.getRelation(), DependantRelations.Child);
        assertEquals(result.getDob(), dob);
        assertEquals(result.getHealthNotes(), healthNotes);
        assertEquals(result.getHealthConcern(), YesNo.No);
    }

    @Test
    @DisplayName("should throw when dependant not found")
    void deleteDependant_shouldThrow_whenDependantNotFound() {
        given(candidateDependantRepository.findById(DEPENDANT_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDependantService.deleteDependant(DEPENDANT_ID));

        assertTrue(ex.getMessage().contains(String.valueOf(DEPENDANT_ID)));
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void deleteDependant_shouldThrow_whenCandidateNotFound() {
        given(candidateDependantRepository.findById(DEPENDANT_ID)).willReturn(Optional.of(dependant));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDependantService.deleteDependant(DEPENDANT_ID));

        assertTrue(ex.getMessage().contains(String.valueOf(CANDIDATE_ID)));
    }

    @Test
    @DisplayName("should delete dependant")
    void deleteDependant_shouldDeleteDependantAndReturnCandidate() {
        given(candidateDependantRepository.findById(DEPENDANT_ID)).willReturn(Optional.of(dependant));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));

        assertEquals(candidateDependantService.deleteDependant(DEPENDANT_ID), candidate); // When
        verify(candidateDependantRepository).deleteById(dependant.getId());
    }

    @Test
    @DisplayName("should return list of dependants")
    void list_shouldReturnDependants() {
        given(candidateDependantRepository.findByCandidateId(CANDIDATE_ID)).willReturn(dependantList);

        assertEquals(candidateDependantService.list(CANDIDATE_ID), dependantList);
    }

    @Test
    @DisplayName("should return empty list when none found")
    void list_shouldReturnEmptyList_whenNoneFound() {
        given(candidateDependantRepository.findByCandidateId(CANDIDATE_ID))
            .willReturn(Collections.emptyList());

        assertTrue(candidateDependantService.list(CANDIDATE_ID).isEmpty());
    }

    @Test
    @DisplayName("should throw when dependant not found")
    void updateIntakeData_shouldThrow_whenDependantNotFound() {
        given(candidateDependantRepository.findById(DEPENDANT_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDependantService.updateIntakeData(candidate, intakeData));

        assertTrue(ex.getMessage().contains(String.valueOf(DEPENDANT_ID)));
    }

    @Test
    @DisplayName("should populate intake data and save dependant")
    void updateIntakeData_shouldPopulateIntakeDataAndSaveDependant() {
        CandidateDependant mockDependant = mock(CandidateDependant.class);
        given(candidateDependantRepository.findById(DEPENDANT_ID))
            .willReturn(Optional.of(mockDependant));

        candidateDependantService.updateIntakeData(candidate, intakeData);

        verify(mockDependant).populateIntakeData(candidate, intakeData);
        verify(candidateDependantRepository).save(mockDependant);
    }

    @Test
    @DisplayName("should return dependant when found")
    void getDependant_shouldReturnDependant_whenFound() {
        given(candidateDependantRepository.findById(DEPENDANT_ID)).willReturn(Optional.of(dependant));

        assertEquals(dependant, candidateDependantService.getDependant(DEPENDANT_ID));
    }

    @Test
    @DisplayName("should throw when dependant not found")
    void getDependant_shouldThrow_whenDependantNotFound() {
        given(candidateDependantRepository.findById(DEPENDANT_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDependantService.getDependant(DEPENDANT_ID));

        assertTrue(ex.getMessage().contains(String.valueOf(DEPENDANT_ID)));
    }

}
