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
import static org.tctalent.server.data.CountryTestData.JORDAN;

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
import org.tctalent.server.model.db.CandidateCitizenship;
import org.tctalent.server.model.db.HasPassport;
import org.tctalent.server.repository.db.CandidateCitizenshipRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;

@ExtendWith(MockitoExtension.class)
class CandidateCitizenshipServiceImplTest {

    private Candidate candidate;
    private CreateCandidateCitizenshipRequest createRequest;
    private CandidateIntakeDataUpdate intakeData;
    private long candidateId;

    private static final long NATIONALITY_ID = 11L;
    private static final long CITIZENSHIP_ID = 133L;

    @Mock private CandidateCitizenshipRepository candidateCitizenshipRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private CountryRepository countryRepository;

    @Captor private ArgumentCaptor<CandidateCitizenship> citizenshipCaptor;

    @InjectMocks
    CandidateCitizenshipServiceImpl candidateCitizenshipService;

    @BeforeEach
    void setUp() {
        candidate = getCandidate();
        candidateId = candidate.getId();
        createRequest = new CreateCandidateCitizenshipRequest();
        createRequest.setNationalityId(NATIONALITY_ID);
        intakeData = new CandidateIntakeDataUpdate();
        intakeData.setCitizenId(CITIZENSHIP_ID);
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void createCitizenship_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(candidateId)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateCitizenshipService.createCitizenship(candidateId, createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(candidateId)));
    }

    @Test
    @DisplayName("should create citizenship as expected")
    void createCitizenship_shouldCreateCitizenship() {
        createRequest.setNationalityId(NATIONALITY_ID);
        final String notes = "notes";
        createRequest.setNotes(notes);
        createRequest.setHasPassport(HasPassport.ValidPassport);

        given(candidateRepository.findById(candidateId)).willReturn(Optional.of(candidate));
        given(countryRepository.findById(NATIONALITY_ID)).willReturn(Optional.of(JORDAN));

        candidateCitizenshipService.createCitizenship(candidateId, createRequest);

        verify(candidateCitizenshipRepository).save(citizenshipCaptor.capture());
        final CandidateCitizenship citizenship = citizenshipCaptor.getValue();
        assertEquals(candidate, citizenship.getCandidate());
        assertEquals(JORDAN, citizenship.getNationality());
        assertEquals(notes, citizenship.getNotes());
        assertEquals(HasPassport.ValidPassport, citizenship.getHasPassport());
    }

    @Test
    @DisplayName("should throw when nationality country not found")
    void createCitizenship_shouldThrow_whenNationalityCountryNotFound() {
        given(candidateRepository.findById(candidateId)).willReturn(Optional.of(candidate));
        given(countryRepository.findById(NATIONALITY_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateCitizenshipService.createCitizenship(candidateId, createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(NATIONALITY_ID)));
    }

    @Test
    @DisplayName("should return true when no exception thrown by delete method")
    void deleteCitizenship_shouldReturnTrue_whenNoExceptionThrown() {
        assertTrue(candidateCitizenshipService.deleteCitizenship(CITIZENSHIP_ID));
    }

    @Test
    @DisplayName("should throw when delete method throws")
    void deleteCitizenship_shouldThrow_whenDeleteMethodThrows() {
        doThrow(new EntityReferencedException("Delete failed"))
            .when(candidateCitizenshipRepository)
            .deleteById(CITIZENSHIP_ID);

        assertThrows(EntityReferencedException.class,
            () -> candidateCitizenshipService.deleteCitizenship(CITIZENSHIP_ID));
    }

    @Test
    @DisplayName("should throw when nationality country not found")
    void updateIntakeData_shouldThrow_whenNationalityCountryNotFound() {
        given(countryRepository.findById(NATIONALITY_ID)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateCitizenshipService.updateIntakeData(NATIONALITY_ID, candidate, intakeData));

        assertTrue(ex.getMessage().contains(String.valueOf(NATIONALITY_ID)));
    }

    @Test
    @DisplayName("should throw when citizenship not found")
    void updateIntakeData_shouldThrow_whenCitizenshipNotFound() {
        given(countryRepository.findById(NATIONALITY_ID)).willReturn(Optional.of(JORDAN));
        given(candidateCitizenshipRepository.findById(intakeData.getCitizenId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateCitizenshipService.updateIntakeData(NATIONALITY_ID, candidate, intakeData));

        assertTrue(ex.getMessage().contains(String.valueOf(CITIZENSHIP_ID)));
    }

    @Test
    @DisplayName("should populate intake data and save citizenship")
    void updateIntakeData_shouldPopulateIntakeDataAndSaveCitizenship() {
        CandidateCitizenship mockCitizenship = mock(CandidateCitizenship.class);
        given(countryRepository.findById(NATIONALITY_ID)).willReturn(Optional.of(JORDAN));
        given(candidateCitizenshipRepository.findById(intakeData.getCitizenId()))
            .willReturn(Optional.ofNullable(mockCitizenship));

        candidateCitizenshipService.updateIntakeData(NATIONALITY_ID, candidate, intakeData);

        verify(mockCitizenship).populateIntakeData(candidate, JORDAN, intakeData);
        verify(candidateCitizenshipRepository).save(mockCitizenship);
    }

}
