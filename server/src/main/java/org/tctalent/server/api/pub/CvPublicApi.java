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

package org.tctalent.server.api.pub;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.security.CandidateTokenProvider;
import org.tctalent.server.security.CvClaims;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.dto.DtoCollectionItemFilter;

/**
 * Public API - accessible without a login
 */
@RestController()
@RequestMapping("/api/public/cv")
public class CvPublicApi {
    private final CandidateService candidateService;
    private final CandidateTokenProvider candidateTokenProvider;
    private final CountryService countryService;
    private final OccupationService occupationService;

    public CvPublicApi(CandidateService candidateService,
        CandidateTokenProvider candidateTokenProvider, CountryService countryService, OccupationService occupationService) {
        this.candidateService = candidateService;
        this.candidateTokenProvider = candidateTokenProvider;
        this.countryService = countryService;
        this.occupationService = occupationService;
    }

    @GetMapping("{token}")
    public Map<String, Object> decodeCvRequest(@PathVariable("token") String token) {

        try {
            CvClaims claims = candidateTokenProvider.getCvClaimsFromToken(token);
            Candidate candidate = candidateService.findByCandidateNumber(claims.candidateNumber());
            if (candidate == null) {
                throw new NoSuchObjectException("Unknown candidate");
            }
            CvBuilder cvBuilder = new CvBuilder(candidate, claims.restrictCandidateOccupations(), claims.candidateOccupationIds());
            return cvBuilder.build();

        } catch (ExpiredJwtException ex) {
            throw new InvalidRequestException("This link has expired");
        } catch (JwtException ex) {
            throw new InvalidRequestException("Invalid link");
        }
    }

    private class CvBuilder {
        private final Candidate candidate;
        private final HashSet<Long> includeIds;
        private final DtoCollectionItemFilter<CandidateOccupation> candidateOccupationFilter;
        private final DtoCollectionItemFilter<CandidateJobExperience> candidateJobExperienceFilter;

        public CvBuilder(@NonNull Candidate candidate, boolean restrictCandidateOccupations, @NonNull List<Long> candidateOccupationIds) {
            this.candidate = candidate;
            includeIds = new HashSet<>(candidateOccupationIds);
            candidateOccupationFilter = restrictCandidateOccupations ?
                    i -> !includeIds.contains(i.getId().longValue()) : null;
            candidateJobExperienceFilter =  restrictCandidateOccupations ?
                    i -> !includeIds.contains(i.getCandidateOccupation().getId().longValue()) :
                    null;
        }

        public Map<String, Object> build() {
            return candidateDto().build(candidate);
        }

        private DtoBuilder candidateDto() {
            return new DtoBuilder()
                    .add("id")
                    .add("candidateNumber")
                    .add("publicId")
                    .add("country", countryService.selectBuilder())
                    .add("candidateOccupations", candidateOccupationDto())
                    .add("candidateJobExperiences", candidateJobExperienceDto())
                    .add("candidateEducations", candidateEducationDto())
                    .add("candidateLanguages", candidateLanguageDto())
                    .add("candidateCertifications", candidateCertificationDto())
                    ;
        }

        private DtoBuilder candidateJobExperienceDto() {
            return new DtoBuilder(this.candidateJobExperienceFilter)
                    .add("id")
                    .add("companyName")
                    .add("role")
                    .add("startDate")
                    .add("endDate")
                    .add("fullTime")
                    .add("paid")
                    .add("description")
                    .add("country", countryService.selectBuilder())
                    .add("candidateOccupation", candidateOccupationDto())
                    ;
        }

        private DtoBuilder candidateOccupationDto() {
            return new DtoBuilder(this.candidateOccupationFilter)
                    .add("id")
                    .add("occupation", occupationService.selectBuilder())
                    .add("yearsExperience")
                    ;
        }

        private DtoBuilder candidateEducationDto() {
            return new DtoBuilder()
                    .add("id")
                    .add("educationType")
                    .add("country", countryService.selectBuilder())
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
                    .add("spokenLevel", languageLevelDto())
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
}
