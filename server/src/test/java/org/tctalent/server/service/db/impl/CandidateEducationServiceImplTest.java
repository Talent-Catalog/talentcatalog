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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.tctalent.server.data.CandidateTestData.getCandidate;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.repository.db.CandidateEducationRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.EducationMajorRepository;
import org.tctalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tctalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class CandidateEducationServiceImplTest {

    private CreateCandidateEducationRequest createRequest;
    private CandidateEducation education;
    private List<CandidateEducation> educationList;
    private UpdateCandidateEducationRequest updateRequest;

    private static final long CANDIDATE_ID = getCandidate().getId();
    private static final long EDUCATION_ID = 33L;
    private static final long COUNTRY_ID = 66L;

    @Mock private CandidateEducationRepository candidateEducationRepository;
    @Mock private CandidateService candidateService;
    @Mock private CountryRepository countryRepository;
    @Mock private EducationMajorRepository educationMajorRepository;
    @Mock private AuthService authService;

    @Captor private ArgumentCaptor<CandidateEducation> educationCaptor;

    @InjectMocks
    CandidateEducationServiceImpl candidateEducationService;

    @BeforeEach
    void setUp() {
        createRequest = new CreateCandidateEducationRequest();
        createRequest.setCountryId(COUNTRY_ID);
        education = new CandidateEducation();
        educationList = List.of(education, education);
        updateRequest = new UpdateCandidateEducationRequest();
    }

    @Test
    @DisplayName("should return list of educations when found")
    void list_shouldReturnListOfEducations_whenFound() {
        given(candidateEducationRepository.findByCandidateId(CANDIDATE_ID))
            .willReturn(educationList);

        assertEquals(educationList, candidateEducationService.list(CANDIDATE_ID));
    }

    @Test
    @DisplayName("should return empty list when none found")
    void list_shouldReturnEmptyList_whenNoneFound() {
        given(candidateEducationRepository.findByCandidateId(CANDIDATE_ID))
            .willReturn(Collections.emptyList());

        assertTrue(candidateEducationService.list(CANDIDATE_ID).isEmpty());
    }

//    @Test
//    @DisplayName("should throw when candidate not found")
//    void createDestination_shouldThrow_whenCandidateNotFound() {
//        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());
//
//        Exception ex = assertThrows(NoSuchObjectException.class,
//            () -> candidateDestinationService.createDestination(CANDIDATE_ID, createRequest));
//
//        assertTrue(ex.getMessage().contains(String.valueOf(CANDIDATE_ID)));
//    }
//
//    @Test
//    @DisplayName("should throw when country not found")
//    void createDestination_shouldThrow_whenCountryNotFound() {
//        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
//        given(countryRepository.findById(COUNTRY_ID)).willReturn(Optional.empty());
//
//        Exception ex = assertThrows(NoSuchObjectException.class,
//            () -> candidateDestinationService.createDestination(CANDIDATE_ID, createRequest));
//
//        assertTrue(ex.getMessage().contains(String.valueOf(COUNTRY_ID)));
//    }
//
//    @Test
//    @DisplayName("should create destination as expected")
//    void createDestination_shouldCreateDestination() {
//        createRequest.setInterest(INTEREST);
//        createRequest.setNotes(NOTES);
//
//        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
//        given(countryRepository.findById(COUNTRY_ID)).willReturn(Optional.of(UNITED_KINGDOM));
//
//        candidateDestinationService.createDestination(CANDIDATE_ID, createRequest);
//
//        verify(candidateDestinationRepository).save(destinationCaptor.capture());
//        CandidateDestination result = destinationCaptor.getValue();
//        assertEquals(result.getCandidate(), CANDIDATE);
//        assertEquals(result.getCountry(), UNITED_KINGDOM);
//        assertEquals(result.getInterest(), INTEREST);
//        assertEquals(result.getNotes(), NOTES);
//        assertEquals(result.getCandidate(), CANDIDATE);
//    }
//
//    @Test
//    @DisplayName("should throw when destination not found")
//    void updateDestination_shouldThrow_whenDestinationNotFound() {
//        given(candidateDestinationRepository.findById(DESTINATION_ID))
//            .willReturn(Optional.empty());
//
//        Exception ex = assertThrows(NoSuchObjectException.class,
//            () -> candidateDestinationService.updateDestination(DESTINATION_ID, updateRequest));
//
//        assertTrue(ex.getMessage().contains(String.valueOf(DESTINATION_ID)));
//    }
//
//    @Test
//    @DisplayName("should update and save destination as expected")
//    void updateDestination_shouldUpdateAndSaveDestination() {
//        updateRequest.setInterest(INTEREST);
//        updateRequest.setNotes(NOTES);
//
//        given(candidateDestinationRepository.findById(DESTINATION_ID))
//            .willReturn(Optional.of(destination));
//
//        candidateDestinationService.updateDestination(DESTINATION_ID, updateRequest);
//
//        verify(candidateDestinationRepository).save(destinationCaptor.capture());
//        CandidateDestination result = destinationCaptor.getValue();
//        assertEquals(result.getNotes(), NOTES);
//        assertEquals(result.getInterest(), INTEREST);
//    }
//
//    @Test
//    @DisplayName("should return true when no exception thrown by delete method")
//    void deleteDestination_shouldReturnTrue_whenNoExceptionThrown() {
//        assertTrue(candidateDestinationService.deleteDestination(DESTINATION_ID));
//    }
//
//    @Test
//    @DisplayName("should throw when delete method throws")
//    void deleteDestination_shouldThrow_whenDeleteMethodThrows() {
//        doThrow(new EntityReferencedException("Delete failed"))
//            .when(candidateDestinationRepository)
//            .deleteById(DESTINATION_ID);
//
//        assertThrows(EntityReferencedException.class,
//            () -> candidateDestinationService.deleteDestination(DESTINATION_ID));
//    }

}
