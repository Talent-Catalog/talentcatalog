package org.tctalent.server.service.db.verify.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.verify.VerifyPlusScanRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.verify.VerifyPlusIngestResult;
import org.tctalent.server.service.db.verify.VerifyPlusPayload;
import org.tctalent.server.service.db.verify.VerifyPlusPayloadParser;

class VerifyPlusServiceImplTest {

    @Mock
    private CandidateService candidateService;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private VerifyPlusPayloadParser payloadParser;

    @InjectMocks
    private VerifyPlusServiceImpl verifyPlusService;

    private Candidate candidate;
    private VerifyPlusScanRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        candidate = new Candidate();
        candidate.setId(10L);

        request = new VerifyPlusScanRequest();
        request.setRawPayload("{\"v\":\"mock-1\",\"unhcrId\":\"UNHCR-1\"}");
    }

    @Test
    @DisplayName("Given a valid payload with a unique UNHCR ID, when ingestScan is called, then the candidate's UNHCR number is updated and duplicate is false")
    void ingestScan_uniqueUnhcrId_persistsAndReturnsDuplicateFalse() {
        VerifyPlusPayload parsed = new VerifyPlusPayload("mock-1", request.getRawPayload(), "UNHCR-1");
        when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
        when(payloadParser.parse(request.getRawPayload())).thenReturn(parsed);
        when(candidateRepository.findOthersByUnhcrNumber(any(), any(), any(Long.class))).thenReturn(
            Collections.emptyList());
        when(candidateService.save(candidate)).thenReturn(candidate);

        VerifyPlusIngestResult result = verifyPlusService.ingestScan(request);

        assertEquals("UNHCR-1", candidate.getUnhcrNumber());
        assertEquals("UNHCR-1", result.getUnhcrNumber());
        assertFalse(result.isDuplicate());
        verify(candidateService).save(candidate);
    }

    @Test
    @DisplayName("Given a valid payload with a duplicate UNHCR ID, when ingestScan is called, then the candidate's UNHCR number is updated and duplicate is true")
    void ingestScan_duplicateUnhcrId_returnsDuplicateTrue() {
        VerifyPlusPayload parsed = new VerifyPlusPayload("mock-1", request.getRawPayload(), "UNHCR-1");
        Candidate other = new Candidate();
        other.setId(20L);

        when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
        when(payloadParser.parse(request.getRawPayload())).thenReturn(parsed);
        when(candidateRepository.findOthersByUnhcrNumber(any(), any(), any(Long.class))).thenReturn(
            List.of(other));
        when(candidateService.save(candidate)).thenReturn(candidate);

        VerifyPlusIngestResult result = verifyPlusService.ingestScan(request);

        assertTrue(result.isDuplicate());
    }

    @Test
    @DisplayName("Given a candidate with an existing UNHCR number, when ingestScan is called with a new UNHCR number, then the candidate's UNHCR number is overwritten")
    void ingestScan_overwritesUnhcrNumberOnRescan() {
        candidate.setUnhcrNumber("OLD-UNHCR");
        VerifyPlusPayload parsed = new VerifyPlusPayload("mock-1", request.getRawPayload(), "NEW-UNHCR");

        when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
        when(payloadParser.parse(request.getRawPayload())).thenReturn(parsed);
        when(candidateRepository.findOthersByUnhcrNumber(any(), any(), any(Long.class))).thenReturn(
            Collections.emptyList());
        when(candidateService.save(candidate)).thenReturn(candidate);

        VerifyPlusIngestResult result = verifyPlusService.ingestScan(request);

        assertEquals("NEW-UNHCR", candidate.getUnhcrNumber());
        assertEquals("NEW-UNHCR", result.getUnhcrNumber());
    }

    @Test
    @DisplayName("Given no logged-in candidate, when ingestScan is called, then an InvalidSessionException is thrown")
    void ingestScan_notLoggedIn_throwsInvalidSessionException() {
        when(candidateService.getLoggedInCandidate()).thenReturn(Optional.empty());

        assertThrows(InvalidSessionException.class, () -> verifyPlusService.ingestScan(request));
    }
}
