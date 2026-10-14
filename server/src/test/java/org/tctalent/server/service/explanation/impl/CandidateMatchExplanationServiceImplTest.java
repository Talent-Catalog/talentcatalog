package org.tctalent.server.service.explanation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.tctalent.server.data.CandidateTestData.getCandidate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.MatchExplanationException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.model.db.MatchingSnapshot;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanation;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanationData;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanationKey;
import org.tctalent.server.model.db.mapper.CandidateMatchingExplanationMapper;
import org.tctalent.server.repository.db.CandidateJobExperienceRepository;
import org.tctalent.server.repository.db.CandidateMatchingExplanationRepository;
import org.tctalent.server.response.CandidateMatchExplanation;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.MatchingService;
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
    private static final long MATCHING_ID = 777L;
    private static final long USER_ID = 99L;
    private static final OffsetDateTime UPDATED_DATE =
        OffsetDateTime.parse("2026-10-01T10:15:30.123456Z");
    private static final OffsetDateTime GENERATED_AT = OffsetDateTime.parse("2026-10-02T03:30:00Z");
    private static final String MODEL_NAME = "qwen.qwen3-235b-a22b-2507-v1:0";

    @Mock private CandidateService candidateService;
    @Mock private MatchingService matchingService;
    @Mock private CandidateJobExperienceRepository candidateJobExperienceRepository;
    @Mock private CandidateMatchingExplanationRepository candidateMatchingExplanationRepository;
    @Mock private CandidateMatchExplanationServiceClient candidateMatchExplanationServiceClient;
    @Mock private AuditorAware<User> auditorAware;

    @Captor private ArgumentCaptor<ExplanationsRequest> requestCaptor;
    @Captor private ArgumentCaptor<String> jsonCaptor;

    private CandidateMatchExplanationServiceImpl service;
    private CandidateMatchingExplanationKey key;

    private Candidate candidate;
    private String candidateIdString;
    private CandidateJobExperience experience1;
    private CandidateJobExperience experience2;

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

        key = new CandidateMatchingExplanationKey(candidate.getId(), MATCHING_ID);

        // The real mapper, so that tests see what would actually be saved and returned.
        service = new CandidateMatchExplanationServiceImpl(candidateService, matchingService,
            candidateJobExperienceRepository, candidateMatchingExplanationRepository,
            Mappers.getMapper(CandidateMatchingExplanationMapper.class),
            candidateMatchExplanationServiceClient, auditorAware);

        // Lenient: defaults which only some tests rely on, or override.
        lenient().when(candidateService.getCandidate(candidate.getId())).thenReturn(candidate);
        lenient().when(matchingService.resolveDescription(MATCHING_ID)).thenReturn(
            new MatchingSnapshot(MATCHING_ID, OPPORTUNITY_DESCRIPTION, UPDATED_DATE));
        lenient().when(candidateMatchingExplanationRepository.findById(key))
            .thenReturn(Optional.empty());
        lenient().when(candidateMatchingExplanationRepository.saveIfMatchingUnchanged(
            anyLong(), anyLong(), any(), any(), any())).thenReturn(1);
        lenient().when(auditorAware.getCurrentAuditor()).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("should return match explanation when Python service succeeds")
    void generateExplanation_shouldReturnExplanation_whenSuccessful() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1));

        ExplanationResult result = ExplanationResult.builder()
            .candidateId(candidateIdString)
            .generatedAt(GENERATED_AT)
            .modelName(MODEL_NAME)
            .summary(SUMMARY)
            .experienceExplanations(List.of(
                ExperienceExplanationItem.builder()
                    .experienceId(String.valueOf(EXPERIENCE_ID_1))
                    // Deliberately differs from the experience's role: the title must come from
                    // the Python response, not be re-derived from the candidate's experience.
                    .jobTitle("Title From Python")
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
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals(GENERATED_AT, explanation.getGeneratedAt());
        assertEquals(MODEL_NAME, explanation.getModelName());
        assertEquals(SUMMARY, explanation.getSummary());
        assertEquals(1, explanation.getExperienceExplanations().size());
        assertEquals(EXPERIENCE_ID_1, explanation.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Title From Python", explanation.getExperienceExplanations().get(0).getJobTitle());
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

        service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

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

        service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

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
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

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
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

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
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

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
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));
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
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

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
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));
    }

    @Test
    @DisplayName("should send the Matching's description, and nothing about jobs, to Python")
    void getOrGenerateExplanation_shouldUseMatchingDescription() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(requestCaptor.capture()))
            .willReturn(successResponseWithNoExperiences());

        service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        verify(matchingService).resolveDescription(MATCHING_ID);
        assertEquals(OPPORTUNITY_DESCRIPTION, requestCaptor.getValue().getOpportunityDescription());
    }

    @Test
    @DisplayName("should save a generated explanation with the snapshot updatedDate and current user")
    void getOrGenerateExplanation_shouldSaveWithSnapshotDate() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());
        User user = new User();
        user.setId(USER_ID);
        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(user));

        CandidateMatchExplanation explanation =
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals(SUMMARY, explanation.getSummary());
        verify(candidateMatchingExplanationRepository).saveIfMatchingUnchanged(
            eq(candidate.getId()), eq(MATCHING_ID), eq(UPDATED_DATE), jsonCaptor.capture(),
            eq(USER_ID));
        String json = jsonCaptor.getValue();
        assertTrue(json.contains("\"summary\":\"" + SUMMARY + "\""), json);
        assertTrue(json.contains("\"generatedAt\":\"2026-10-02T03:30:00Z\""), json);
        assertTrue(json.contains("\"modelName\":\"" + MODEL_NAME + "\""), json);
    }

    @Test
    @DisplayName("should return a current cached explanation without calling Python")
    void getOrGenerateExplanation_shouldReturnCurrentCachedExplanation() {
        given(candidateMatchingExplanationRepository.findById(key))
            .willReturn(Optional.of(cachedExplanation(UPDATED_DATE, "Cached summary")));

        CandidateMatchExplanation explanation =
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals("Cached summary", explanation.getSummary());
        verifyNoInteractions(candidateMatchExplanationServiceClient);
        verify(candidateMatchingExplanationRepository, never())
            .saveIfMatchingUnchanged(anyLong(), anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("should treat equal instants with different offsets as current")
    void getOrGenerateExplanation_shouldCompareUpdatedDatesAsInstants() {
        given(candidateMatchingExplanationRepository.findById(key))
            .willReturn(Optional.of(cachedExplanation(
                UPDATED_DATE.withOffsetSameInstant(ZoneOffset.ofHours(10)), "Cached summary")));

        CandidateMatchExplanation explanation =
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals("Cached summary", explanation.getSummary());
        verifyNoInteractions(candidateMatchExplanationServiceClient);
    }

    @Test
    @DisplayName("should regenerate a stale cached explanation, replacing it")
    void getOrGenerateExplanation_shouldRegenerateStaleExplanation() {
        given(candidateMatchingExplanationRepository.findById(key))
            .willReturn(Optional.of(cachedExplanation(UPDATED_DATE.minusNanos(1000), "Old")));
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());

        CandidateMatchExplanation explanation =
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals(SUMMARY, explanation.getSummary());
        verify(candidateMatchingExplanationRepository).saveIfMatchingUnchanged(
            eq(candidate.getId()), eq(MATCHING_ID), eq(UPDATED_DATE), any(), any());
    }

    @Test
    @DisplayName("regenerate should ignore a current cached explanation and replace it")
    void regenerateExplanation_shouldIgnoreCurrentCachedExplanation() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());

        CandidateMatchExplanation explanation =
            service.regenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals(SUMMARY, explanation.getSummary());
        verify(candidateMatchingExplanationRepository, never()).findById(any());
        verify(candidateMatchingExplanationRepository).saveIfMatchingUnchanged(
            eq(candidate.getId()), eq(MATCHING_ID), eq(UPDATED_DATE), any(), any());
    }

    @Test
    @DisplayName("should reject a Matching with no description without calling Python")
    void getOrGenerateExplanation_shouldThrow_whenNoDescription() {
        given(matchingService.resolveDescription(MATCHING_ID))
            .willReturn(new MatchingSnapshot(MATCHING_ID, null, UPDATED_DATE));

        assertThrows(InvalidRequestException.class,
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

        verifyNoInteractions(candidateMatchExplanationServiceClient);
        verify(candidateMatchingExplanationRepository, never())
            .saveIfMatchingUnchanged(anyLong(), anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("should reject a Matching whose description was cleared after an explanation was generated")
    void getOrGenerateExplanation_shouldThrow_whenDescriptionClearedAfterGeneration() {
        //Clearing the description changed updatedDate, so the cached explanation is stale.
        given(matchingService.resolveDescription(MATCHING_ID))
            .willReturn(new MatchingSnapshot(MATCHING_ID, null, UPDATED_DATE));
        given(candidateMatchingExplanationRepository.findById(key))
            .willReturn(Optional.of(cachedExplanation(UPDATED_DATE.minusDays(1), "Old")));

        assertThrows(InvalidRequestException.class,
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));
    }

    @Test
    @DisplayName("should not save an explanation that fails validation")
    void getOrGenerateExplanation_shouldNotSave_whenValidationFails() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1));
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithExperiences(EXPERIENCE_ID_2));

        assertThrows(MatchExplanationException.class,
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

        verify(candidateMatchingExplanationRepository, never())
            .saveIfMatchingUnchanged(anyLong(), anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("should discard an explanation if the Matching changed during generation, and retry once")
    void getOrGenerateExplanation_shouldRetry_whenMatchingChangedDuringGeneration() {
        final OffsetDateTime changedDate = UPDATED_DATE.plusSeconds(5);
        given(matchingService.resolveDescription(MATCHING_ID)).willReturn(
            new MatchingSnapshot(MATCHING_ID, OPPORTUNITY_DESCRIPTION, UPDATED_DATE),
            new MatchingSnapshot(MATCHING_ID, "Changed description", changedDate));
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(requestCaptor.capture()))
            .willReturn(successResponseWithNoExperiences());
        given(candidateMatchingExplanationRepository.saveIfMatchingUnchanged(
            anyLong(), anyLong(), eq(UPDATED_DATE), any(), any())).willReturn(0);

        CandidateMatchExplanation explanation =
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals(SUMMARY, explanation.getSummary());
        assertEquals(List.of(OPPORTUNITY_DESCRIPTION, "Changed description"),
            requestCaptor.getAllValues().stream()
                .map(ExplanationsRequest::getOpportunityDescription).toList());
        verify(candidateMatchingExplanationRepository).saveIfMatchingUnchanged(
            eq(candidate.getId()), eq(MATCHING_ID), eq(changedDate), any(), any());
    }

    @Test
    @DisplayName("should fail, saving nothing, if the Matching keeps changing during generation")
    void getOrGenerateExplanation_shouldThrow_whenMatchingKeepsChanging() {
        given(matchingService.resolveDescription(MATCHING_ID)).willReturn(
            new MatchingSnapshot(MATCHING_ID, "Description 1", UPDATED_DATE),
            new MatchingSnapshot(MATCHING_ID, "Description 2", UPDATED_DATE.plusSeconds(5)));
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());
        given(candidateMatchingExplanationRepository.saveIfMatchingUnchanged(
            anyLong(), anyLong(), any(), any(), any())).willReturn(0);

        assertThrows(MatchExplanationException.class,
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

        verify(candidateMatchExplanationServiceClient,
            times(CandidateMatchExplanationServiceImpl.MAX_GENERATION_ATTEMPTS))
            .generateExplanations(any());
    }

    @Test
    @DisplayName("regenerate should return an explanation of the new description saved meanwhile by another request")
    void regenerateExplanation_shouldUseExplanationSavedMeanwhile_whenMatchingChanged() {
        final OffsetDateTime changedDate = UPDATED_DATE.plusSeconds(5);
        given(matchingService.resolveDescription(MATCHING_ID)).willReturn(
            new MatchingSnapshot(MATCHING_ID, OPPORTUNITY_DESCRIPTION, UPDATED_DATE),
            new MatchingSnapshot(MATCHING_ID, "Changed description", changedDate));
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of());
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithNoExperiences());
        given(candidateMatchingExplanationRepository.saveIfMatchingUnchanged(
            anyLong(), anyLong(), any(), any(), any())).willReturn(0);
        given(candidateMatchingExplanationRepository.findById(key))
            .willReturn(Optional.of(cachedExplanation(changedDate, "Saved meanwhile")));

        CandidateMatchExplanation explanation =
            service.regenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals("Saved meanwhile", explanation.getSummary());
        verify(candidateMatchExplanationServiceClient, times(1)).generateExplanations(any());
    }

    @Test
    @DisplayName("should throw, without calling Python, when the Matching doesn't exist")
    void getOrGenerateExplanation_shouldThrow_whenNoSuchMatching() {
        given(matchingService.resolveDescription(MATCHING_ID))
            .willThrow(new NoSuchObjectException(Matching.class, MATCHING_ID));

        assertThrows(NoSuchObjectException.class,
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

        verifyNoInteractions(candidateMatchExplanationServiceClient);
    }

    @Test
    @DisplayName("should accept when every sent experience ID is returned exactly once")
    void generateExplanation_shouldAccept_whenAllSentExperienceIdsReturnedExactlyOnce() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1, experience2));
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithExperiences(EXPERIENCE_ID_1, EXPERIENCE_ID_2));

        CandidateMatchExplanation explanation =
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

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
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

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
            () -> service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID));

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
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertEquals(SUMMARY, explanation.getSummary());
        assertTrue(explanation.getExperienceExplanations().isEmpty());
    }

    @Test
    @DisplayName("should propagate null jobTitle and metadata as null rather than fabricating values")
    void generateExplanation_shouldNotFabricateMissingMetadata() {
        given(candidateJobExperienceRepository.findByCandidateId(candidate.getId()))
            .willReturn(List.of(experience1));
        given(candidateMatchExplanationServiceClient.generateExplanations(any()))
            .willReturn(successResponseWithExperiences(EXPERIENCE_ID_1));

        CandidateMatchExplanation explanation =
            service.getOrGenerateExplanation(candidate.getId(), MATCHING_ID);

        assertNull(explanation.getGeneratedAt());
        assertNull(explanation.getModelName());
        assertNull(explanation.getExperienceExplanations().get(0).getJobTitle());
    }

    private ExplanationsResponse successResponseWithNoExperiences() {
        return ExplanationsResponse.builder()
            .requested(1).succeeded(1).failed(0)
            .results(List.of(ExplanationResult.builder()
                .candidateId(candidateIdString)
                .generatedAt(GENERATED_AT)
                .modelName(MODEL_NAME)
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

    /** A cached explanation generated when the Matching's updatedDate was the given date. */
    private CandidateMatchingExplanation cachedExplanation(
        OffsetDateTime matchingUpdatedDate, String summary) {
        CandidateMatchingExplanation entity =
            BeanUtils.instantiateClass(CandidateMatchingExplanation.class);
        ReflectionTestUtils.setField(entity, "id", key);
        ReflectionTestUtils.setField(entity, "matchingUpdatedDate", matchingUpdatedDate);
        ReflectionTestUtils.setField(entity, "explanation",
            CandidateMatchingExplanationData.builder().summary(summary).build());
        return entity;
    }
}
