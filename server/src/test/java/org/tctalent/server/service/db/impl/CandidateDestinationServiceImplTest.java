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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.CountryTestData.UNITED_KINGDOM;

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
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.repository.db.CandidateDestinationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.destination.CreateCandidateDestinationRequest;
import org.tctalent.server.request.candidate.destination.UpdateCandidateDestinationRequest;

@ExtendWith(MockitoExtension.class)
class CandidateDestinationServiceImplTest {

    private CreateCandidateDestinationRequest createRequest;
    private CandidateDestination destination;
    private List<CandidateDestination> destinationList;
    private CandidateIntakeDataUpdate intakeData;
    private UpdateCandidateDestinationRequest updateRequest;

    private static final long CANDIDATE_ID = getCandidate().getId();
    private static final long DESTINATION_ID = 33L;
    private static final long COUNTRY_ID = 66L;
    private static final String NOTES = "notes";
    private static final YesNoUnsure INTEREST = YesNoUnsure.Yes;
    private static final Candidate CANDIDATE = getCandidate();

    @Mock private CandidateDestinationRepository candidateDestinationRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private CountryRepository countryRepository;

    @Captor private ArgumentCaptor<CandidateDestination> destinationCaptor;

    @InjectMocks
    CandidateDestinationServiceImpl candidateDestinationService;

    @BeforeEach
    void setUp() {
        createRequest = new CreateCandidateDestinationRequest();
        createRequest.setCountryId(COUNTRY_ID);
        destination = new CandidateDestination();
        destination.setId(DESTINATION_ID);
        updateRequest = new UpdateCandidateDestinationRequest();
        destinationList = List.of(destination, destination);
        intakeData = new CandidateIntakeDataUpdate();
        intakeData.setDestinationId(destination.getId());
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void createDestination_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDestinationService.createDestination(CANDIDATE_ID, createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(CANDIDATE_ID)));
    }

    @Test
    @DisplayName("should throw when country not found")
    void createDestination_shouldThrow_whenCountryNotFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
        given(countryRepository.findById(COUNTRY_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDestinationService.createDestination(CANDIDATE_ID, createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(COUNTRY_ID)));
    }

    @Test
    @DisplayName("should create destination as expected")
    void createDestination_shouldCreateDestination() {
        createRequest.setInterest(INTEREST);
        createRequest.setNotes(NOTES);

        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
        given(countryRepository.findById(COUNTRY_ID)).willReturn(Optional.of(UNITED_KINGDOM));

        candidateDestinationService.createDestination(CANDIDATE_ID, createRequest);

        verify(candidateDestinationRepository).save(destinationCaptor.capture());
        CandidateDestination result = destinationCaptor.getValue();
        assertEquals(CANDIDATE, result.getCandidate());
        assertEquals(UNITED_KINGDOM, result.getCountry());
        assertEquals(INTEREST, result.getInterest());
        assertEquals(NOTES, result.getNotes());
        assertEquals(CANDIDATE, result.getCandidate());
    }

    @Test
    @DisplayName("should throw when destination not found")
    void updateDestination_shouldThrow_whenDestinationNotFound() {
        given(candidateDestinationRepository.findById(DESTINATION_ID))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDestinationService.updateDestination(DESTINATION_ID, updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(DESTINATION_ID)));
    }

    @Test
    @DisplayName("should update and save destination as expected")
    void updateDestination_shouldUpdateAndSaveDestination() {
        updateRequest.setInterest(INTEREST);
        updateRequest.setNotes(NOTES);

        given(candidateDestinationRepository.findById(DESTINATION_ID))
            .willReturn(Optional.of(destination));

        candidateDestinationService.updateDestination(DESTINATION_ID, updateRequest);

        verify(candidateDestinationRepository).save(destinationCaptor.capture());
        CandidateDestination result = destinationCaptor.getValue();
        assertEquals(NOTES, result.getNotes());
        assertEquals(INTEREST, result.getInterest());
    }

    @Test
    @DisplayName("should return true when no exception thrown by delete method")
    void deleteDestination_shouldReturnTrue_whenNoExceptionThrown() {
        assertTrue(candidateDestinationService.deleteDestination(DESTINATION_ID));
    }

    @Test
    @DisplayName("should throw when delete method throws")
    void deleteDestination_shouldThrow_whenDeleteMethodThrows() {
        doThrow(new EntityReferencedException("Delete failed"))
            .when(candidateDestinationRepository)
            .deleteById(DESTINATION_ID);

        assertThrows(EntityReferencedException.class,
            () -> candidateDestinationService.deleteDestination(DESTINATION_ID));
    }

    @Test
    @DisplayName("should throw when country not found")
    void updateIntakeData_shouldThrow_whenCountryNotFound() {
        given(countryRepository.findById(COUNTRY_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDestinationService.updateIntakeData(COUNTRY_ID, CANDIDATE, intakeData));

        assertTrue(ex.getMessage().contains(String.valueOf(COUNTRY_ID)));
    }

    @Test
    @DisplayName("should throw when destination not found")
    void updateIntakeData_shouldThrow_whenDestinationNotFound() {
        given(countryRepository.findById(COUNTRY_ID)).willReturn(Optional.of(UNITED_KINGDOM));
        given(candidateDestinationRepository.findById(DESTINATION_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateDestinationService.updateIntakeData(COUNTRY_ID, CANDIDATE, intakeData));

        assertTrue(ex.getMessage().contains(String.valueOf(DESTINATION_ID)));
    }

    @Test
    @DisplayName("should populate intake data and save destination")
    void updateIntakeData_shouldPopulateIntakeDataAndSaveDestination() {
        given(countryRepository.findById(COUNTRY_ID)).willReturn(Optional.of(UNITED_KINGDOM));
        CandidateDestination mockDestination = mock(CandidateDestination.class);
        given(candidateDestinationRepository.findById(DESTINATION_ID))
            .willReturn(Optional.of(mockDestination));

        candidateDestinationService.updateIntakeData(COUNTRY_ID, CANDIDATE, intakeData);

        verify(mockDestination).populateIntakeData(CANDIDATE, UNITED_KINGDOM, intakeData);
        verify(candidateDestinationRepository).save(mockDestination);
    }

    @Test
    @DisplayName("should return list of destinations when found")
    void list_shouldReturnListOfDestinations_whenFound() {
        given(candidateDestinationRepository.findByCandidateId(CANDIDATE_ID))
            .willReturn(destinationList);

        assertEquals(destinationList, candidateDestinationService.list(CANDIDATE_ID));
    }

    @Test
    @DisplayName("should return empty list when none found")
    void list_shouldReturnEmptyList_whenNoneFound() {
        given(candidateDestinationRepository.findByCandidateId(CANDIDATE_ID))
            .willReturn(Collections.emptyList());

        assertTrue(candidateDestinationService.list(CANDIDATE_ID).isEmpty());
    }

}
