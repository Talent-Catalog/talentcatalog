package org.tctalent.server.service.explanation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.tctalent.server.data.CandidateTestData.getCandidate;

import java.util.Arrays;
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
import org.springframework.web.client.RestClientException;
import org.tctalent.server.exception.MatchExplanationException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanation;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanationData;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanationKey;
import org.tctalent.server.model.db.mapper.CandidateJobMatchExplanationMapper;
import org.tctalent.server.repository.db.CandidateJobExperienceRepository;
import org.tctalent.server.repository.db.CandidateJobMatchExplanationRepository;
import org.tctalent.server.response.CandidateMatchExplanation;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.explanation.dto.CandidateMatchExplanationServiceClient;
import org.tctalent.server.service.explanation.dto.ExperienceExplanationItem;
import org.tctalent.server.service.explanation.dto.ExplanationCandidateInput;
import org.tctalent.server.service.explanation.dto.ExplanationError;
import org.tctalent.server.service.explanation.dto.ExplanationResult;
import org.tctalent.server.service.explanation.dto.ExplanationsRequest;
import org.tctalent.server.service.explanation.dto.ExplanationsResponse;

@ExtendWith(MockitoExtension.class)
class CandidateMatchExplanationServiceImplTest {

    private static final long EXPERIENCE_ID_1 = 501L;
    private static final long EXPERIENCE_ID_2 = 502L;
    private static final String ROLE_1 = "Senior Programmer";
    private static final String DESCRIPTION_1 = "Wrote backend services.";
    private static final String ROLE_2 = "Team Lead";
    private static final String DESCRIPTION_2 = "Led a team of five engineers.";
    private static final String OPPORTUNITY_DESCRIPTION =
        "We are looking for an experienced Java developer.";
    private static final String SUMMARY = "The candidate is a strong match.";
    private static final long JOB_ID = 777L;

    @Mock private CandidateService candidateService;
    @Mock private SalesforceJobOppService salesforceJobOppService;
    @Mock private CandidateJobExperienceRepository candidateJobExperienceRepository;
    @Mock private CandidateJobMatchExplanationRepository candidateJobMatchExplanationRepository;
    @Mock private CandidateJobMatchExplanationMapper candidateJobMatchExplanationMapper;
    @Mock private CandidateMatchExplanationServiceClient candidateMatchExplanationServiceClient;

    @Captor private ArgumentCaptor<ExplanationsRequest> requestCaptor;
    @Captor private ArgumentCaptor<CandidateJobMatchExplanation> entityCaptor;

    @InjectMocks
    private CandidateMatchExplanationServiceImpl service;

    private Candidate candidate;
    private String candidateIdString;
    private CandidateJobExperience experience1;
    private CandidateJobExperience experience2;
    private SalesforceJobOpp job;

    @BeforeEach
    void setUp() {
        candidate = getCandidate();
        candidateIdString = String.valueOf(candidate.getId());

        experience1 = new CandidateJobExperience();
        experience1.setId(EXPERIENCE_ID_1);
        experience1.setRole(ROLE_1);
        experience1.setDescription(DESCRIPTION_1);

        experience2 = new CandidateJobExperience();
        experience2.setId(EXPERIENCE_ID_2);
        experience2.setRole(ROLE_2);
        experience2.setDescription(DESCRIPTION_2);

        job = new SalesforceJobOpp();
        job.setId(JOB_ID);

        // Lenient: not every test (e.g. the getPersistedExplanation tests) exercises this path.
        lenient().when(candidateService.getCandidate(candidate.getId())).thenReturn(candidate);
    }

    @Test
    @DisplayName("should return match explanation when Python service succeeds")
    void generateExplanation_shouldReturnExplanation_whenSuccessful() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1));

        ExplanationResult result = ExplanationResult.builder()
            .candidateId(candidateIdString)
            .summary(SUMMARY)
            .experienceExplanations(List.of(
                ExperienceExplanationItem.builder()
                    .experienceId(String.valueOf(EXPERIENCE_ID_1))
                    .explanation("Directly relevant Java experience.")
                    .build()
            ))
            .limitations(List.of("Limited detail in job description."))
            .build();
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(ExplanationsResponse.builder()
                .requested(1).succeeded(1).failed(0)
                .results(List.of(result))
                .build());

        CandidateMatchExplanation explanation =
            service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION);

        assertEquals(SUMMARY, explanation.getSummary());
        assertEquals(1, explanation.getExperienceExplanations().size());
        assertEquals(EXPERIENCE_ID_1, explanation.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Directly relevant Java experience.",
            explanation.getExperienceExplanations().get(0).getExplanation());
        assertEquals(List.of("Limited detail in job description."), explanation.getLimitations());
    }

    @Test
    @DisplayName("should map TC candidate ID to string in the Python request")
    void generateExplanation_shouldMapCandidateId() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(requestCaptor.capture()))
            .willReturn(successResponseWithNoExperiences());

        service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION);

        ExplanationCandidateInput candidateInput = requestCaptor.getValue().getCandidates().get(0);
        assertEquals(candidateIdString, candidateInput.getCandidateId());
        assertEquals(OPPORTUNITY_DESCRIPTION, requestCaptor.getValue().getOpportunityDescription());
    }

    @Test
    @DisplayName("should map all of a candidate's job experiences into the Python request")
    void generateExplanation_shouldMapMultipleJobExperiences() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1, experience2));
        given(candidateMatchExplanationServiceClient.generateExplanations(requestCaptor.capture()))
            .willReturn(successResponseWithExperiences(EXPERIENCE_ID_1, EXPERIENCE_ID_2));

        service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION);

        var experiences = requestCaptor.getValue().getCandidates().get(0).getExperiences();
        assertEquals(2, experiences.size());

        assertEquals(String.valueOf(EXPERIENCE_ID_1), experiences.get(0).getExperienceId());
        assertEquals(ROLE_1, experiences.get(0).getJobTitle());
        assertEquals(DESCRIPTION_1, experiences.get(0).getDescription());

        assertEquals(String.valueOf(EXPERIENCE_ID_2), experiences.get(1).getExperienceId());
        assertEquals(ROLE_2, experiences.get(1).getJobTitle());
        assertEquals(DESCRIPTION_2, experiences.get(1).getDescription());
    }

    @Test
    @DisplayName("should map Python experience IDs back to TC Long IDs")
    void generateExplanation_shouldMapExperienceIdsBackToLong() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1, experience2));

        ExplanationResult result = ExplanationResult.builder()
            .candidateId(candidateIdString)
            .summary(SUMMARY)
            .experienceExplanations(List.of(
                ExperienceExplanationItem.builder()
                    .experienceId(String.valueOf(EXPERIENCE_ID_1))
                    .explanation("Explanation 1")
                    .build(),
                ExperienceExplanationItem.builder()
                    .experienceId(String.valueOf(EXPERIENCE_ID_2))
                    .explanation("Explanation 2")
                    .build()
            ))
            .build();
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(ExplanationsResponse.builder()
                .requested(1).succeeded(1).failed(0)
                .results(List.of(result))
                .build());

        CandidateMatchExplanation explanation =
            service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION);

        assertEquals(EXPERIENCE_ID_1, explanation.getExperienceExplanations().get(0).getExperienceId());
        assertEquals(EXPERIENCE_ID_2, explanation.getExperienceExplanations().get(1).getExperienceId());
    }

    @Test
    @DisplayName("should throw when Python reports an item-level failure")
    void generateExplanation_shouldThrow_whenItemLevelError() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());

        ExplanationResult result = ExplanationResult.builder()
            .candidateId(candidateIdString)
            .error(ExplanationError.builder()
                .code("LLM_SERVICE_UNAVAILABLE")
                .message("The LLM service is unavailable")
                .build())
            .build();
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(ExplanationsResponse.builder()
                .requested(1).succeeded(0).failed(1)
                .results(List.of(result))
                .build());

        Exception ex = assertThrows(MatchExplanationException.class,
            () -> service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION));

        assertTrue(ex.getMessage().contains("LLM_SERVICE_UNAVAILABLE"));
        assertTrue(ex.getMessage().contains("The LLM service is unavailable"));
    }

    @Test
    @DisplayName("should throw when Python returns an experience ID that was not sent")
    void generateExplanation_shouldThrow_whenReturnedExperienceIdUnexpected() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1));

        ExplanationResult result = ExplanationResult.builder()
            .candidateId(candidateIdString)
            .summary(SUMMARY)
            .experienceExplanations(List.of(
                ExperienceExplanationItem.builder()
                    .experienceId("not-a-sent-id")
                    .explanation("Explanation for unknown experience")
                    .build()
            ))
            .build();
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(ExplanationsResponse.builder()
                .requested(1).succeeded(1).failed(0)
                .results(List.of(result))
                .build());

        Exception ex = assertThrows(MatchExplanationException.class,
            () -> service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION));

        assertTrue(ex.getMessage().contains("not-a-sent-id"));
    }

    @Test
    @DisplayName("should throw when Python returns no results")
    void generateExplanation_shouldThrow_whenNoResults() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(ExplanationsResponse.builder()
                .requested(1).succeeded(0).failed(0)
                .results(List.of())
                .build());

        assertThrows(MatchExplanationException.class,
            () -> service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION));
    }

    @Test
    @DisplayName("should throw when the returned candidate ID does not match the requested candidate")
    void generateExplanation_shouldThrow_whenCandidateIdMismatched() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());

        ExplanationResult result = ExplanationResult.builder()
            .candidateId("not-" + candidateIdString)
            .summary(SUMMARY)
            .build();
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(ExplanationsResponse.builder()
                .requested(1).succeeded(1).failed(0)
                .results(List.of(result))
                .build());

        Exception ex = assertThrows(MatchExplanationException.class,
            () -> service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION));

        assertTrue(ex.getMessage().contains(candidateIdString));
    }

    @Test
    @DisplayName("should throw when the Python service call fails")
    void generateExplanation_shouldThrow_whenServiceCallFails() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willThrow(new RestClientException("connection refused"));

        assertThrows(MatchExplanationException.class,
            () -> service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION));
    }

    @Test
    @DisplayName("POST without jobId: generates and returns the explanation but persists nothing")
    void generateExplanation_shouldNotPersist_whenJobIdAbsent() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());

        CandidateMatchExplanation explanation =
            service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION);

        assertEquals(SUMMARY, explanation.getSummary());
        verifyNoInteractions(
            salesforceJobOppService, candidateJobMatchExplanationRepository,
            candidateJobMatchExplanationMapper);
    }

    @Test
    @DisplayName("POST with valid jobId: loads the job, generates, persists against (candidate, job), and returns")
    void generateExplanation_shouldPersist_whenJobIdSupplied() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(salesforceJobOppService.getJobOpp(JOB_ID)).willReturn(job);
        given(candidateMatchExplanationServiceClient.generateExplanations(requestCaptor.capture()))
            .willReturn(successResponseWithNoExperiences());
        given(candidateJobMatchExplanationRepository.findById(any())).willReturn(Optional.empty());

        CandidateJobMatchExplanationData mappedData =
            CandidateJobMatchExplanationData.builder().summary(SUMMARY).build();
        given(candidateJobMatchExplanationMapper.toData(any(CandidateMatchExplanation.class)))
            .willReturn(mappedData);

        CandidateMatchExplanation explanation =
            service.generateExplanation(candidate.getId(), JOB_ID, OPPORTUNITY_DESCRIPTION);

        assertEquals(SUMMARY, explanation.getSummary());
        // The Python request has no notion of a TC job at all - confirm the job ID is nowhere in it.
        assertFalse(requestCaptor.getValue().toString().contains(String.valueOf(JOB_ID)));

        verify(candidateJobMatchExplanationRepository)
            .findById(new CandidateJobMatchExplanationKey(candidate.getId(), JOB_ID));
        verify(candidateJobMatchExplanationRepository).save(entityCaptor.capture());
        CandidateJobMatchExplanation savedEntity = entityCaptor.getValue();
        assertEquals(candidate, savedEntity.getCandidate());
        assertEquals(job, savedEntity.getJob());
        assertEquals(mappedData, savedEntity.getExplanation());
    }

    @Test
    @DisplayName("POST with same candidate/job when a persisted explanation already exists: updates it, no duplicate")
    void generateExplanation_shouldUpdateExistingRecord_whenAlreadyPersisted() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(salesforceJobOppService.getJobOpp(JOB_ID)).willReturn(job);
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());

        CandidateJobMatchExplanation existingEntity = new CandidateJobMatchExplanation(candidate, job);
        existingEntity.setExplanation(
            CandidateJobMatchExplanationData.builder().summary("Old summary").build());
        given(candidateJobMatchExplanationRepository.findById(any()))
            .willReturn(Optional.of(existingEntity));

        CandidateJobMatchExplanationData newData =
            CandidateJobMatchExplanationData.builder().summary(SUMMARY).build();
        given(candidateJobMatchExplanationMapper.toData(any(CandidateMatchExplanation.class)))
            .willReturn(newData);

        service.generateExplanation(candidate.getId(), JOB_ID, OPPORTUNITY_DESCRIPTION);

        verify(candidateJobMatchExplanationRepository, times(1)).save(entityCaptor.capture());
        assertSame(existingEntity, entityCaptor.getValue());
        assertEquals(newData, existingEntity.getExplanation());
    }

    @Test
    @DisplayName("POST with a nonexistent jobId: throws before calling Python or persisting anything")
    void generateExplanation_shouldThrow_whenJobIdDoesNotExist() {
        given(salesforceJobOppService.getJobOpp(JOB_ID))
            .willThrow(new NoSuchObjectException(SalesforceJobOpp.class, JOB_ID));

        assertThrows(NoSuchObjectException.class,
            () -> service.generateExplanation(candidate.getId(), JOB_ID, OPPORTUNITY_DESCRIPTION));

        verifyNoInteractions(
            candidateMatchExplanationServiceClient, candidateJobMatchExplanationRepository,
            candidateJobMatchExplanationMapper);
        verify(candidateJobExperienceRepository, never()).findByCandidateId(any());
    }

    @Test
    @DisplayName("GET: returns the persisted explanation without calling Python")
    void getPersistedExplanation_shouldReturnPersisted_whenExists() {
        CandidateJobMatchExplanation entity = new CandidateJobMatchExplanation(candidate, job);
        CandidateJobMatchExplanationData data =
            CandidateJobMatchExplanationData.builder().summary(SUMMARY).build();
        entity.setExplanation(data);

        given(candidateJobMatchExplanationRepository.findById(
            new CandidateJobMatchExplanationKey(candidate.getId(), JOB_ID)))
            .willReturn(Optional.of(entity));

        CandidateMatchExplanation expected =
            CandidateMatchExplanation.builder().summary(SUMMARY).build();
        given(candidateJobMatchExplanationMapper.toResponse(data)).willReturn(expected);

        CandidateMatchExplanation result =
            service.getPersistedExplanation(candidate.getId(), JOB_ID);

        assertEquals(expected, result);
        verifyNoInteractions(
            candidateMatchExplanationServiceClient, candidateService, salesforceJobOppService,
            candidateJobExperienceRepository);
    }

    @Test
    @DisplayName("GET: throws 404-mapped exception when no explanation has been persisted")
    void getPersistedExplanation_shouldThrow_whenMissing() {
        given(candidateJobMatchExplanationRepository.findById(any())).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> service.getPersistedExplanation(candidate.getId(), JOB_ID));
    }

    @Test
    @DisplayName("should accept when every sent experience ID is returned exactly once")
    void generateExplanation_shouldAccept_whenAllSentExperienceIdsReturnedExactlyOnce() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1, experience2));
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithExperiences(EXPERIENCE_ID_1, EXPERIENCE_ID_2));

        CandidateMatchExplanation explanation =
            service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION);

        assertEquals(2, explanation.getExperienceExplanations().size());
    }

    @Test
    @DisplayName("should reject when a supplied experience is omitted from the response")
    void generateExplanation_shouldThrow_whenSuppliedExperienceOmitted() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1, experience2));
        // Only experience1 is echoed back - experience2 is missing.
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithExperiences(EXPERIENCE_ID_1));

        Exception ex = assertThrows(MatchExplanationException.class,
            () -> service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION));

        assertTrue(ex.getMessage().contains(String.valueOf(EXPERIENCE_ID_2)));
    }

    @Test
    @DisplayName("should reject when the same experience ID is returned more than once")
    void generateExplanation_shouldThrow_whenExperienceIdReturnedTwice() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1, experience2));
        // experience1's ID is returned twice; experience2's ID never appears.
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithExperiences(EXPERIENCE_ID_1, EXPERIENCE_ID_1));

        Exception ex = assertThrows(MatchExplanationException.class,
            () -> service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION));

        assertTrue(ex.getMessage().contains(String.valueOf(EXPERIENCE_ID_1)));
    }

    @Test
    @DisplayName("should accept when no experiences were sent and none were returned")
    void generateExplanation_shouldAccept_whenNoExperiencesSentAndNoneReturned() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());

        CandidateMatchExplanation explanation =
            service.generateExplanation(candidate.getId(), null, OPPORTUNITY_DESCRIPTION);

        assertEquals(SUMMARY, explanation.getSummary());
        assertTrue(explanation.getExperienceExplanations().isEmpty());
    }

    private ExplanationsResponse successResponseWithNoExperiences() {
        return ExplanationsResponse.builder()
            .requested(1).succeeded(1).failed(0)
            .results(List.of(ExplanationResult.builder()
                .candidateId(candidateIdString)
                .summary(SUMMARY)
                .build()))
            .build();
    }

    /** Builds a successful response echoing back one experience explanation per given ID. */
    private ExplanationsResponse successResponseWithExperiences(long... experienceIds) {
        List<ExperienceExplanationItem> items = Arrays.stream(experienceIds)
            .mapToObj(id -> ExperienceExplanationItem.builder()
                .experienceId(String.valueOf(id))
                .explanation("Explanation for " + id)
                .build())
            .toList();

        return ExplanationsResponse.builder()
            .requested(1).succeeded(1).failed(0)
            .results(List.of(ExplanationResult.builder()
                .candidateId(candidateIdString)
                .summary(SUMMARY)
                .experienceExplanations(items)
                .build()))
            .build();
    }
}
