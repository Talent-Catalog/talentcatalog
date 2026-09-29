package org.tctalent.server.service.explanation.impl;

import java.util.List;
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

        List<ExplanationExperienceInput> experienceInputs = experiences.stream()
            .map(experience -> ExplanationExperienceInput.builder()
                .experienceId(String.valueOf(experience.getId()))
                .jobTitle(experience.getRole())
                .description(experience.getDescription())
                .build())
            .toList();

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
            LogBuilder.builder(log)
                .candidateId(candidate.getId())
                .action("generateExplanation")
                .message("Match explanation service reported an error: " + result.getError())
                .logError();
            throw new MatchExplanationException(
                "Match explanation service reported an error for candidate " + candidateIdString
                    + ": " + result.getError());
        }

        List<ExperienceMatchExplanation> experienceExplanations =
            result.getExperienceExplanations() == null
                ? List.of()
                : result.getExperienceExplanations().stream()
                    .map(item -> ExperienceMatchExplanation.builder()
                        .experienceId(Long.valueOf(item.getExperienceId()))
                        .explanation(item.getExplanation())
                        .build())
                    .toList();

        return CandidateMatchExplanation.builder()
            .summary(result.getSummary())
            .experienceExplanations(experienceExplanations)
            .limitations(result.getLimitations())
            .build();
    }
}
