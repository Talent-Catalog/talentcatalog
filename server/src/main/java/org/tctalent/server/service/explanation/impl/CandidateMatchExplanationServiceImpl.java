package org.tctalent.server.service.explanation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.tctalent.server.exception.MatchExplanationException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.repository.db.CandidateJobExperienceRepository;
import org.tctalent.server.response.CandidateMatchExplanation;
import org.tctalent.server.response.ExperienceMatchExplanation;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.explanation.CandidateMatchExplanationService;
import org.tctalent.server.service.explanation.dto.CandidateMatchExplanationServiceClient;
import org.tctalent.server.service.explanation.dto.ExplanationCandidateInput;
import org.tctalent.server.service.explanation.dto.ExplanationError;
import org.tctalent.server.service.explanation.dto.ExplanationExperienceInput;
import org.tctalent.server.service.explanation.dto.ExplanationResult;
import org.tctalent.server.service.explanation.dto.ExplanationsRequest;
import org.tctalent.server.service.explanation.dto.ExplanationsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateMatchExplanationServiceImpl implements CandidateMatchExplanationService {

    private final CandidateService candidateService;
    private final CandidateJobExperienceRepository candidateJobExperienceRepository;
    private final CandidateMatchExplanationServiceClient candidateMatchExplanationServiceClient;

    @Override
    public @NonNull CandidateMatchExplanation generateExplanation(
        long candidateId, @NonNull String opportunityDescription)
        throws NoSuchObjectException, MatchExplanationException {

        Candidate candidate = candidateService.getCandidate(candidateId);

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
                .message("Call to match explanation service failed")
                .logError(e);
            throw new MatchExplanationException(
                "Failed to call match explanation service for candidate " + candidateIdString, e);
        }

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Match explanation service returned no result")
                .logError();
            throw new MatchExplanationException(
                "Match explanation service returned no result for candidate " + candidateIdString);
        }

        ExplanationResult result = response.getResults().get(0);

        if (!candidateIdString.equals(result.getCandidateId())) {
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Match explanation service returned mismatched candidate ID: "
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
                .message("Match explanation service reported an error: ["
                    + error.getCode() + "] " + error.getMessage())
                .logError();
            throw new MatchExplanationException(
                "Match explanation service reported an error for candidate " + candidateIdString
                    + ": [" + error.getCode() + "] " + error.getMessage());
        }

        List<ExperienceMatchExplanation> experienceExplanations =
            result.getExperienceExplanations() == null
                ? List.of()
                : result.getExperienceExplanations().stream()
                    .map(item -> {
                        Long experienceId = sentExperienceIdsByString.get(item.getExperienceId());
                        if (experienceId == null) {
                            throw new MatchExplanationException(
                                "Match explanation service returned unknown experience ID '"
                                    + item.getExperienceId() + "' for candidate "
                                    + candidateIdString);
                        }
                        return ExperienceMatchExplanation.builder()
                            .experienceId(experienceId)
                            .explanation(item.getExplanation())
                            .build();
                    })
                    .toList();

        return CandidateMatchExplanation.builder()
            .summary(result.getSummary())
            .experienceExplanations(experienceExplanations)
            .limitations(result.getLimitations())
            .build();
    }
}
