/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tctalent.server.configuration.properties.VectorEmbeddingModelProperties;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.repository.db.AlternateJobExperienceEmbeddingRepository;
import org.tctalent.server.repository.db.CandidateJobExperienceRepository;
import org.tctalent.server.repository.db.CandidateOccupationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.EmbeddingModelRepository;
import org.tctalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tctalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tctalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateJobExperienceService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SkillsService;
import org.tctalent.server.service.embedding.TcVectorEmbeddingService;
import org.tctalent.server.service.embedding.dto.EmbeddingResult;
import org.tctalent.server.service.embedding.dto.GenerateEmbeddingsResponse;
import org.tctalent.server.util.text.TextParts;
import org.tctalent.server.util.text.TextPartsCodec;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateJobExperienceServiceImpl implements CandidateJobExperienceService {

    private final AlternateJobExperienceEmbeddingRepository altRepo;
    private final CandidateJobExperienceRepository candidateJobExperienceRepository;
    private final CountryRepository countryRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final CandidateOccupationRepository candidateOccupationRepository;
    private final EmbeddingModelRepository embeddingModelRepository;
    private final AuthService authService;
    private final SkillsService skillsService;
    private final TcVectorEmbeddingService tcVectorEmbeddingService;
    private final VectorEmbeddingModelProperties vectorEmbeddingModelProperties;

    @Override
    public Page<CandidateJobExperience> searchCandidateJobExperience(
        SearchJobExperienceRequest request) {
        if (request.getActiveCandidate() != null && request.getActiveCandidate()) {
            return candidateJobExperienceRepository.findByActiveCandidate(request.getPageRequest());
        } else if (request.getCandidateId() != null) {
            return candidateJobExperienceRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
        } else {
            return candidateJobExperienceRepository.findByCandidateOccupationId(request.getCandidateOccupationId(), request.getPageRequest());
        }
    }

    @Override
    public CandidateJobExperience createCandidateJobExperience(CreateJobExperienceRequest request) {
        Candidate candidate;
        /* Check if the candidate ID is explicitly set - this means the request is coming from admin */
        if (request.getCandidateId() != null) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
        } else {
            candidate = candidateService.getLoggedInCandidate()
                    .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        }

        // Load the country from the database - throw an exception if not found
        Country country = getCountry(request.getCountryId());

        // Load the candidate occupation from the database - throw an exception if not found
        CandidateOccupation occupation = getCandidateOccupation(request.getCandidateOccupationId());

        // Create a new candidateOccupation object to insert into the database
        CandidateJobExperience candidateJobExperience = new CandidateJobExperience();
        candidateJobExperience.setCandidate(candidate);
        candidateJobExperience.setCountry(country);
        candidateJobExperience.setCandidateOccupation(occupation);
        candidateJobExperience.setCompanyName(request.getCompanyName());
        candidateJobExperience.setRole(request.getRole());
        candidateJobExperience.setStartDate(request.getStartDate());
        candidateJobExperience.setEndDate(request.getEndDate());
        candidateJobExperience.setFullTime(request.getFullTime());
        candidateJobExperience.setPaid(request.getPaid());
        updateJobExperienceDescription(candidateJobExperience, request.getDescription());

        // Save the candidateOccupation
        final CandidateJobExperience jobExperience = candidateJobExperienceRepository.save(candidateJobExperience);

        //Save the candidate
        candidateService.save(candidate, true);

        return jobExperience;
    }

    @Override
    public CandidateJobExperience updateCandidateJobExperience(UpdateJobExperienceRequest request) {
        Candidate candidate = authService.getLoggedInCandidate();
        if (candidate == null) {
            throw new InvalidSessionException("Not logged in");
        }
        CandidateJobExperience experience = updateCandidateJobExperience(request.getId(), request);

        return experience;
    }

    @Override
    public CandidateJobExperience updateCandidateJobExperience(Long id, UpdateJobExperienceRequest request) {
        // Load the candidate from the database - throw an exception if not found
        CandidateJobExperience candidateJobExperience = candidateJobExperienceRepository
                .findByIdLoadCandidateOccupation(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateJobExperience.class, id));

        Country country = getCountry(request.getCountryId());

        // Default to the existing candidate occupation
        CandidateOccupation candidateOccupation = candidateJobExperience.getCandidateOccupation();

        // Check if the candidate occupation needs to be updated
        if (request.getCandidateOccupationId() != null) {
            if (candidateOccupation == null || !candidateOccupation.getId().equals(request.getCandidateOccupationId())) {
                candidateOccupation = getCandidateOccupation(request.getCandidateOccupationId());
            }
        }

        // Update the database record
        candidateJobExperience.setCountry(country);
        candidateJobExperience.setCompanyName(request.getCompanyName());
        candidateJobExperience.setRole(request.getRole());
        candidateJobExperience.setStartDate(request.getStartDate());
        candidateJobExperience.setEndDate(request.getEndDate());
        candidateJobExperience.setFullTime(request.getFullTime());
        candidateJobExperience.setPaid(request.getPaid());
        updateJobExperienceDescription(candidateJobExperience, request.getDescription());
        candidateJobExperience.setCandidateOccupation(candidateOccupation);

        // Save the candidate experience
        candidateJobExperience = candidateJobExperienceRepository.save(candidateJobExperience);

        Candidate candidate = candidateJobExperience.getCandidate();

        candidateService.save(candidate, true);

        return candidateJobExperience;
    }

    @Override
    public void updateCandidateJobExperienceEmbeddings(List<CandidateJobExperience> experiences) {

        final String tableName = vectorEmbeddingModelProperties.getAlternateEmbeddingTable();
        final String modelKey = vectorEmbeddingModelProperties.getEmbeddingModelKey();
        final EmbeddingModel model = embeddingModelRepository.findByModelKey(
            modelKey);
        Map<String, String> descriptionsById = new HashMap<>();
        experiences.forEach(experience -> {
            descriptionsById.put(experience.getId().toString(), experience.getDescription());
        });

        final GenerateEmbeddingsResponse response =
            tcVectorEmbeddingService.generateEmbeddings(modelKey, descriptionsById);

        final List<EmbeddingResult> results = response.getResults();
        for (EmbeddingResult result : results) {
            if (result.isSuccessful()) {
                altRepo.upsert(tableName,
                    Long.parseLong(result.getId()), model.getId(), result.getEmbedding());
            } else {
                log.warn("Error generating embeddings for candidate job experience: {}", result.getError());
            }
        }
    }

    /**
     * Updates the description of a CandidateJobExperience object.
     * <p>
     * It also checks for any additional keywords that have been specified that may need to be
     * stored as new skills.
     * @param candidateJobExperience Job experience to update
     * @param description New description
     */
    private void updateJobExperienceDescription(
        CandidateJobExperience candidateJobExperience, String description) {
        //Extract any keywords.
        TextParts textParts = TextPartsCodec.read(description);
        final List<String> keywords = textParts.getKeywords();
        //Add any new skills to the database.
        skillsService.addTcSkillsIfNew(keywords, "en");
        candidateJobExperience.setDescription(description);
    }

    @Override
    public void deleteCandidateJobExperience(Long id) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateJobExperience candidateJobExperience = candidateJobExperienceRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateJobExperience.class, id));

        Candidate candidate;

        // If request is coming from admin portal
        if (authService.hasAdminPrivileges(user.getRole())) {
            candidate = candidateRepository.findById(candidateJobExperience.getCandidate().getId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateJobExperience.getCandidate().getId()));
        } else {
            candidate = candidateService.getLoggedInCandidate()
                    .orElseThrow(() -> new InvalidSessionException("Not logged in"));
            // Check that the user is deleting their own attachment
            if (!candidate.getId().equals(candidateJobExperience.getCandidate().getId())) {
                throw new InvalidCredentialsException("You do not have permission to perform that action");
            }
        }

        candidateJobExperienceRepository.delete(candidateJobExperience);

        candidateService.save(candidate, true);
    }

    // Load the country from the database - throw an exception if not found
    private Country getCountry(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new NoSuchObjectException(Country.class, countryId));
    }

    // Load the candidate occupation from the database - throw an exception if not found
    private CandidateOccupation getCandidateOccupation(Long candidateOccupationId) {
        return candidateOccupationRepository.findById(candidateOccupationId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, candidateOccupationId));
    }
}
