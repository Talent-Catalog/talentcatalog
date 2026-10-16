package org.tctalent.server.service.explanation.impl;

import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.MatchExplanationException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.MatchingSnapshot;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanation;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanationKey;
import org.tctalent.server.model.db.mapper.CandidateMatchingExplanationMapper;
import org.tctalent.server.repository.db.CandidateJobExperienceRepository;
import org.tctalent.server.repository.db.CandidateMatchingExplanationRepository;
import org.tctalent.server.response.CandidateMatchExplanation;
import org.tctalent.server.response.ExperienceMatchExplanation;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.MatchingService;
import org.tctalent.server.service.explanation.CandidateMatchExplanationService;
import org.tctalent.server.service.explanation.dto.CandidateMatchExplanationServiceClient;
import org.tctalent.server.service.explanation.dto.ExperienceExplanationItem;
import org.tctalent.server.service.explanation.dto.ExplanationCandidateInput;
import org.tctalent.server.service.explanation.dto.ExplanationError;
import org.tctalent.server.service.explanation.dto.ExplanationExperienceInput;
import org.tctalent.server.service.explanation.dto.ExplanationResult;
import org.tctalent.server.service.explanation.dto.ExplanationsRequest;
import org.tctalent.server.service.explanation.dto.ExplanationsResponse;

/**
 * Implementation of {@link CandidateMatchExplanationService}.
 * <p>
 * No database transaction is held open while the explanation service (an LLM) is called. Instead,
 * the Matching's description and updatedDate are read together before generation, and the
 * generated explanation is only saved if the Matching's updatedDate is still the same - see
 * {@link CandidateMatchingExplanationRepository#saveIfMatchingUnchanged}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateMatchExplanationServiceImpl implements CandidateMatchExplanationService {

    /**
     * Maximum number of times an explanation is generated for one request. If the Matching's
     * description changes while an explanation is being generated, the explanation is discarded
     * and generated again from the new description - but only this many times in total.
     */
    static final int MAX_GENERATION_ATTEMPTS = 2;

    private final CandidateService candidateService;
    private final MatchingService matchingService;
    private final CandidateJobExperienceRepository candidateJobExperienceRepository;
    private final CandidateMatchingExplanationRepository candidateMatchingExplanationRepository;
    private final CandidateMatchingExplanationMapper candidateMatchingExplanationMapper;
    private final CandidateMatchExplanationServiceClient candidateMatchExplanationServiceClient;
    private final AuditorAware<User> auditorAware;

    @Override
    public @NonNull CandidateMatchExplanation getOrGenerateExplanation(
        long candidateId, long matchingId)
        throws NoSuchObjectException, InvalidRequestException, MatchExplanationException {
        return getOrGenerate(candidateId, matchingId, false);
    }

    @Override
    public @NonNull CandidateMatchExplanation regenerateExplanation(
        long candidateId, long matchingId)
        throws NoSuchObjectException, InvalidRequestException, MatchExplanationException {
        return getOrGenerate(candidateId, matchingId, true);
    }

    private @NonNull CandidateMatchExplanation getOrGenerate(
        long candidateId, long matchingId, boolean regenerate) {

        final Candidate candidate = candidateService.getCandidate(candidateId);
        final CandidateMatchingExplanationKey key =
            new CandidateMatchingExplanationKey(candidate.getId(), matchingId);

        boolean useCachedExplanation = !regenerate;
        for (int attempt = 1; ; attempt++) {
            //Read the description and updatedDate together, before generating anything.
            final MatchingSnapshot snapshot = matchingService.resolveDescription(matchingId);

            if (useCachedExplanation) {
                final CandidateMatchingExplanation cached =
                    candidateMatchingExplanationRepository.findById(key).orElse(null);
                if (cached != null && cached.isCurrent(snapshot.updatedDate())) {
                    return candidateMatchingExplanationMapper.toResponse(cached.getExplanation());
                }
            }

            final String description = snapshot.matchingDescription();
            if (description == null) {
                throw new InvalidRequestException("Matching " + matchingId
                    + " has no description to explain candidate matches against");
            }

            final CandidateMatchExplanation explanation =
                generateExplanation(candidate, matchingId, description);

            final int saved = candidateMatchingExplanationRepository.saveIfMatchingUnchanged(
                candidate.getId(), matchingId, snapshot.updatedDate(),
                ObjectMapperWrapper.INSTANCE.toString(
                    candidateMatchingExplanationMapper.toData(explanation)),
                auditorAware.getCurrentAuditor().map(User::getId).orElse(null));
            if (saved > 0) {
                return explanation;
            }

            //The Matching's description changed during generation, so the explanation is out of
            //date. Discard it rather than return an explanation of a description which is no
            //longer current.
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("getOrGenerateExplanation")
                .message("Matching " + matchingId + " changed while generating explanation"
                    + " (attempt " + attempt + " of " + MAX_GENERATION_ATTEMPTS + ")")
                .logWarn();
            if (attempt >= MAX_GENERATION_ATTEMPTS) {
                throw new MatchExplanationException("The matching description changed while the "
                    + "match explanation was being generated. Please try again.");
            }

            //An explanation of the new description saved meanwhile (eg by another request) was
            //generated after this request started, so it is good enough even when regenerating.
            useCachedExplanation = true;
        }
    }

    /**
     * Generates an explanation of the candidate's match against the given description, and
     * validates the explanation service's response.
     */
    private @NonNull CandidateMatchExplanation generateExplanation(
        Candidate candidate, long matchingId, String opportunityDescription) {

        List<CandidateJobExperience> experiences =
            candidateJobExperienceRepository.findByCandidateId(candidate.getId());

        String candidateIdString = String.valueOf(candidate.getId());

        // Maps the experience ID strings sent to Python back to the TC Long IDs they came from,
        // so that any experience ID Python echoes back can be validated against what was actually
        // sent rather than blindly parsed.
        Map<String, Long> sentExperienceIdsByString = new HashMap<>();
        List<ExplanationExperienceInput> experienceInputs = new ArrayList<>();
        for (CandidateJobExperience experience : experiences) {
            String experienceIdString = String.valueOf(experience.getId());
            sentExperienceIdsByString.put(experienceIdString, experience.getId());
            experienceInputs.add(ExplanationExperienceInput.builder()
                .experienceId(experienceIdString)
                .jobTitle(experience.getRole())
                .description(experience.getDescription())
                .build());
        }

        ExplanationsRequest request = ExplanationsRequest.builder()
            .opportunityDescription(opportunityDescription)
            .candidates(List.of(
                ExplanationCandidateInput.builder()
                    .candidateId(candidateIdString)
                    .experiences(experienceInputs)
                    .build()
            ))
            .build();

        ExplanationsResponse response;
        try {
            response = candidateMatchExplanationServiceClient.generateExplanations(request);
        } catch (RestClientException e) {
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Matching " + matchingId + ": "
                    + "Call to match explanation service failed")
                .logError(e);
            throw new MatchExplanationException(
                "Failed to call match explanation service for candidate " + candidateIdString, e);
        }

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Matching " + matchingId + ": "
                    + "Match explanation service returned no result")
                .logError();
            throw new MatchExplanationException(
                "Match explanation service returned no result for candidate " + candidateIdString);
        }

        ExplanationResult result = response.getResults().get(0);

        if (!candidateIdString.equals(result.getCandidateId())) {
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Matching " + matchingId + ": "
                    + "Match explanation service returned mismatched candidate ID: "
                    + result.getCandidateId())
                .logError();
            throw new MatchExplanationException(
                "Match explanation service returned mismatched candidate ID. Expected "
                    + candidateIdString + " but got " + result.getCandidateId());
        }

        if (result.getError() != null) {
            ExplanationError error = result.getError();
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Matching " + matchingId + ": "
                    + "Match explanation service reported an error: ["
                    + error.getCode() + "] " + error.getMessage())
                .logError();
            throw new MatchExplanationException(
                "Match explanation service reported an error for candidate " + candidateIdString
                    + ": [" + error.getCode() + "] " + error.getMessage());
        }

        List<ExperienceExplanationItem> returnedItems = result.getExperienceExplanations() == null
            ? List.of()
            : result.getExperienceExplanations();

        // Python is expected to return exactly one experience_explanation for every candidate
        // experience supplied - no fewer, no more, no duplicates. Validate this fully before
        // constructing (and possibly persisting) the final explanation, rather than only
        // checking for unexpected IDs.
        Set<String> seenExperienceIds = new HashSet<>();
        Set<String> duplicateExperienceIds = new HashSet<>();
        for (ExperienceExplanationItem item : returnedItems) {
            if (!seenExperienceIds.add(item.getExperienceId())) {
                duplicateExperienceIds.add(item.getExperienceId());
            }
        }
        if (!duplicateExperienceIds.isEmpty()) {
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Matching " + matchingId + ": "
                    + "Match explanation service returned duplicate experience ID(s): "
                    + duplicateExperienceIds)
                .logError();
            throw new MatchExplanationException(
                "Match explanation service returned duplicate experience ID(s) "
                    + duplicateExperienceIds + " for candidate " + candidateIdString);
        }

        Set<String> sentExperienceIds = sentExperienceIdsByString.keySet();
        if (!seenExperienceIds.equals(sentExperienceIds)) {
            Set<String> missingExperienceIds = new HashSet<>(sentExperienceIds);
            missingExperienceIds.removeAll(seenExperienceIds);
            Set<String> unexpectedExperienceIds = new HashSet<>(seenExperienceIds);
            unexpectedExperienceIds.removeAll(sentExperienceIds);

            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Matching " + matchingId + ": "
                    + "Match explanation service returned experience explanations that do "
                    + "not match the experiences sent. Missing: " + missingExperienceIds
                    + ", unexpected: " + unexpectedExperienceIds)
                .logError();
            throw new MatchExplanationException(
                "Match explanation service returned experience explanations that do not match "
                    + "the experiences sent for candidate " + candidateIdString
                    + ". Missing experience ID(s): " + missingExperienceIds
                    + ". Unexpected experience ID(s): " + unexpectedExperienceIds);
        }

        List<ExperienceMatchExplanation> experienceExplanations = returnedItems.stream()
            .map(item -> ExperienceMatchExplanation.builder()
                .experienceId(sentExperienceIdsByString.get(item.getExperienceId()))
                .jobTitle(item.getJobTitle())
                .explanation(item.getExplanation())
                .build())
            .toList();

        // generatedAt and modelName describe the generated artifact, so they are taken as-is from
        // the Python response rather than recreated here.
        CandidateMatchExplanation explanation = CandidateMatchExplanation.builder()
            .generatedAt(result.getGeneratedAt())
            .modelName(result.getModelName())
            .summary(result.getSummary())
            .experienceExplanations(experienceExplanations)
            .limitations(result.getLimitations())
            .build();

        return explanation;
    }
}
