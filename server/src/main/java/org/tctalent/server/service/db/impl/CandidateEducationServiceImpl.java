/*
 * Copyright (c) 2024 Talent Catalog.
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

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateEducationRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.EducationMajorRepository;
import org.tctalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tctalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateEducationService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.audit.AuditHelper;

@Service
@AllArgsConstructor
public class CandidateEducationServiceImpl implements CandidateEducationService {

    private final CandidateEducationRepository candidateEducationRepository;
    private final CountryRepository countryRepository;
    private final EducationMajorRepository educationMajorRepository;
    private final CandidateService candidateService;
    private final AuthService authService;

    @Override
    public List<CandidateEducation> list(long id) {
        return candidateEducationRepository.findByCandidateId(id);
    }

    @Override
    public CandidateEducation createCandidateEducation(CreateCandidateEducationRequest request) {
        User loggedInUser = authService.getLoggedInUser()
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

        AuditHelper.setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateEducation;
    }

    @Override
    public CandidateEducation updateCandidateEducation(UpdateCandidateEducationRequest request) {
        User loggedInUser = authService.getLoggedInUser()
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
        AuditHelper.setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateEducation;
    }

    @Override
    public void deleteCandidateEducation(Long id) throws UnauthorisedActionException {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateEducation candidateEducation = candidateEducationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));
        Candidate candidate = candidateEducation.getCandidate();

        if (authService.authoriseLoggedInUser(candidate)) {
            candidateEducationRepository.delete(candidateEducation);
            AuditHelper.setAuditFieldsFromUser(candidate, loggedInUser);
            candidateService.save(candidate, true);
        } else {
            throw new UnauthorisedActionException("delete");
        }
    }

}
