/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.repository.db.CandidateEducationRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.EducationMajorRepository;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateEducationService;
import org.tbbtalent.server.service.db.CandidateService;

import java.util.List;

import static org.tbbtalent.server.util.audit.AuditHelper.setAuditFieldsFromUser;

@Service
public class CandidateEducationServiceImpl implements CandidateEducationService {

    private final CandidateEducationRepository candidateEducationRepository;
    private final CountryRepository countryRepository;
    private final EducationMajorRepository educationMajorRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final UserContext userContext;

    @Autowired
    public CandidateEducationServiceImpl(CandidateEducationRepository candidateEducationRepository,
                                         CountryRepository countryRepository,
                                         EducationMajorRepository educationMajorRepository,
                                         CandidateRepository candidateRepository,
                                         CandidateService candidateService,
                                         UserContext userContext) {
        this.candidateEducationRepository = candidateEducationRepository;
        this.countryRepository = countryRepository;
        this.educationMajorRepository = educationMajorRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.userContext = userContext;
    }

    @Override
    public List<CandidateEducation> list(long id) {
        return candidateEducationRepository.findByCandidateId(id);
    }

    @Override
    public CandidateEducation createCandidateEducation(CreateCandidateEducationRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Candidate candidate = candidateService.getCandidateFromRequest(request.getCandidateId());

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        EducationMajor educationMajor = null;
        if (request.getEducationMajorId() != null) {
            educationMajor = educationMajorRepository.findById(request.getEducationMajorId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationMajor.class, request.getEducationMajorId()));
        }

        // Get ENUM for education type
        EducationType educationType = request.getEducationType();

        // Create a new candidateOccupation object to insert into the database
        CandidateEducation candidateEducation = new CandidateEducation();
        candidateEducation.setCandidate(candidate);
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setEducationMajor(educationMajor);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setYearCompleted(request.getYearCompleted());
        candidateEducation.setIncomplete(request.getIncomplete());

        // Save the candidate education
        candidateEducation = candidateEducationRepository.save(candidateEducation);

        setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateEducation;
    }

    @Override
    public CandidateEducation updateCandidateEducation(UpdateCandidateEducationRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        // Get ENUM for education type
        EducationType educationType = request.getEducationType();

        CandidateEducation candidateEducation = candidateEducationRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, request.getId()));

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        EducationMajor educationMajor = educationMajorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new NoSuchObjectException(EducationMajor.class, request.getMajorId()));

        // Update education object to insert into the database
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setEducationMajor(educationMajor);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setYearCompleted(request.getYearCompleted());
        candidateEducation.setIncomplete(request.getIncomplete());

        // Save the candidateEducation
        candidateEducation = candidateEducationRepository.save(candidateEducation);

        Candidate candidate = candidateEducation.getCandidate();
        setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateEducation;
    }

    @Override
    public void deleteCandidateEducation(Long id) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateEducation candidateEducation = candidateEducationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));
        Candidate candidate = candidateEducation.getCandidate();
        candidateEducationRepository.delete(candidateEducation);

        setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);
    }

}
