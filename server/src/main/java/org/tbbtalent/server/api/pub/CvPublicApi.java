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

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * Public API - accessible without a login
 */
@RestController()
@RequestMapping("/api/public/cv")
public class CvPublicApi {
    private final CandidateService candidateService;

    public CvPublicApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping("{token}")
    public Map<String, Object> decodeCvRequest(@PathVariable("token") String token) {

        String candidateNumber = extractCandidateNumber(token);

        Candidate candidate = candidateService.findByCandidateNumber(candidateNumber);
        if (candidate == null) {
            throw new NoSuchObjectException("Unknown candidate");
        }

        return candidateDto().build(candidate);
    }

    private String extractCandidateNumber(String token) {
        //TODO JC Do proper extraction
        return token;
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

            ;
    }

}
