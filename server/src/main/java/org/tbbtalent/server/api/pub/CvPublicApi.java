/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.pub;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.security.CandidateTokenProvider;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.Map;

/**
 * Public API - accessible without a login
 */
@RestController()
@RequestMapping("/api/public/cv")
public class CvPublicApi {
    private final CandidateService candidateService;
    private final CandidateTokenProvider candidateTokenProvider;

    public CvPublicApi(CandidateService candidateService,
        CandidateTokenProvider candidateTokenProvider) {
        this.candidateService = candidateService;
        this.candidateTokenProvider = candidateTokenProvider;
    }

    @GetMapping("{token}")
    public Map<String, Object> decodeCvRequest(@PathVariable("token") String token) {

        try {
            String candidateNumber = candidateTokenProvider.getCandidateNumberFromToken(token);
            Candidate candidate = candidateService.findByCandidateNumber(candidateNumber);
            if (candidate == null) {
                throw new NoSuchObjectException("Unknown candidate");
            }
            return candidateDto().build(candidate);
        } catch (ExpiredJwtException ex) {
            throw new InvalidRequestException("This link has expired");
        } catch (JwtException ex) {
            throw new InvalidRequestException("Invalid link");
        }
    }

    private DtoBuilder candidateDto() {
        return new DtoBuilder()
            .add("id")
            .add("status")
            .add("candidateNumber")
            .add("gender")
            .add("dob")
            .add("phone")
            .add("whatsapp")
            .add("city")
            .add("state")
            .add("address1")
            .add("yearOfArrival")
            .add("externalId")
            .add("externalIdSource")
            .add("unhcrRegistered")
            .add("unhcrNumber")
            .add("unhcrConsent")
            .add("additionalInfo")
            .add("linkedInLink")
            .add("candidateMessage")
            .add("folderlink")
            .add("sflink")
            .add("videolink")
            .add("unhcrStatus")
            .add("unhcrNumber")
            .add("surveyComment")
            .add("selected")
            .add("createdDate")
            .add("updatedDate")
            .add("contextNote")
            .add("maritalStatus")
            .add("drivingLicense")
            .add("langAssessmentScore")
            .add("residenceStatus")
            .add("ieltsScore")
            .add("numberDependants")
            .add("shareableNotes")
            .add("stage")
            .add("sfOpportunityLink")
            .add("country", countryDto())
            .add("candidateOccupations", candidateOccupationDto())
            .add("candidateJobExperiences", candidateJobExperienceDto())
            .add("candidateEducations", candidateEducationDto())
            .add("candidateLanguages", candidateLanguageDto())
            .add("candidateCertifications", candidateCertificationDto())
            ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name");
    }

    private DtoBuilder candidateJobExperienceDto() {
        return new DtoBuilder()
                .add("id")
                .add("companyName")
                .add("role")
                .add("startDate")
                .add("endDate")
                .add("fullTime")
                .add("paid")
                .add("description")
                .add("country", countryDto())
                .add("candidateOccupation", candidateOccupationDto())
                ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationDto())
                .add("yearsExperience")
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder candidateEducationDto() {
        return new DtoBuilder()
                .add("id")
                .add("educationType")
                .add("country", countryDto())
                .add("educationMajor", majorDto())
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("yearCompleted")
                .add("incomplete")
                ;
    }

    private DtoBuilder majorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

    private DtoBuilder candidateLanguageDto() {
        return new DtoBuilder()
                .add("id")
                .add("migrationLanguage")
                .add("language", languageDto())
                .add("writtenLevel", languageLevelDto())
                .add("spokenLevel",languageLevelDto())
                ;
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                ;
    }

    private DtoBuilder candidateCertificationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("institution")
                .add("dateCompleted")
                ;
    }

}
