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

package org.tbbtalent.server.api.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.TaskDtoHelper;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.TaskAssignmentService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate")
public class CandidatePortalApi {

    private final CandidateService candidateService;
    private final TaskAssignmentService taskAssignmentService;

    @Autowired
    public CandidatePortalApi(CandidateService candidateService,
        TaskAssignmentService taskAssignmentService) {
        this.candidateService = candidateService;
        this.taskAssignmentService = taskAssignmentService;
    }

    @GetMapping("contact")
    public Map<String, Object> getCandidateEmail() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateContactDto().build(candidate);
    }

    @PostMapping("contact")
    public Map<String, Object> updateCandidateEmail(@Valid @RequestBody UpdateCandidateContactRequest request) {
        Candidate candidate = this.candidateService.updateContact(request);
        return candidateContactDto().build(candidate);
    }

    @GetMapping("personal")
    public Map<String, Object> getCandidatePersonal() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidatePersonalDto().build(candidate);
    }

    @PostMapping("personal")
    public Map<String, Object> updateCandidatePersonal(@Valid @RequestBody UpdateCandidatePersonalRequest request) {
        Candidate candidate = this.candidateService.updatePersonal(request);
        return candidatePersonalDto().build(candidate);
    }

    @GetMapping("occupation")
    public Map<String, Object> getCandidateCandidateOccupations() {
        Candidate candidate = this.candidateService
                .getLoggedInCandidateLoadCandidateOccupations()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateWithCandidateOccupationsDto().build(candidate);
    }

    @GetMapping("education")
    public Map<String, Object> getCandidateEducation() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateWithEducationDto().build(candidate);
    }

    @PostMapping("education")
    public Map<String, Object> updateCandidateEducationLevel(@Valid @RequestBody UpdateCandidateEducationRequest request) {
        Candidate candidate = this.candidateService.updateEducation(request);
        return candidateEducationLevelDto().build(candidate);
    }

    @GetMapping("languages")
    public Map<String, Object> getCandidateLanguages() {
        Candidate candidate = this.candidateService
                .getLoggedInCandidateLoadCandidateLanguages()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateWithCandidateLanguagesDto().build(candidate);
    }

    @GetMapping("additional-info")
    public Map<String, Object> getCandidateAdditionalInfo() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateAdditionalInfoDto().build(candidate);
    }

    @PostMapping("additional-info")
    public Map<String, Object> updateCandidateAdditionalInfo(@Valid @RequestBody UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = this.candidateService.updateAdditionalInfo(request);
        return candidateAdditionalInfoDto().build(candidate);
    }

    @GetMapping("survey")
    public Map<String, Object> getCandidateSurvey() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateSurveyDto().build(candidate);
    }

    @PostMapping("survey")
    public Map<String, Object> updateCandidateSurvey(@Valid @RequestBody UpdateCandidateSurveyRequest request) {
        Candidate candidate = this.candidateService.updateCandidateSurvey(request);
        return candidateSurveyDto().build(candidate);
    }

    @GetMapping("job-experiences")
    public Map<String, Object> getCandidateJobExperiences() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateWithJobExperiencesDto().build(candidate);
    }

    @GetMapping("certifications")
    public Map<String, Object> getCandidateCertifications() {
        Candidate candidate = this.candidateService
                .getLoggedInCandidateLoadCertifications()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateWithCertificationsDto().build(candidate);
    }

    @GetMapping("status")
    public Map<String, Object> getCandidateStatus() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateStatusDto().build(candidate);
    }

    @GetMapping("profile")
    public Map<String, Object> getCandidateProfile() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateProfileDto().build(candidate);
    }

    @GetMapping("candidate-number")
    public Map<String, Object> getCandidateNumber() {
        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateNumberDto().build(candidate);
    }

    @GetMapping(value = "cv.pdf")
    public void getCandidateCV(HttpServletResponse response)
            throws IOException {

        Candidate candidate = this.candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        String name = candidate.getUser().getDisplayName()+"-"+ "CV";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + name + ".pdf");

        Resource report = candidateService.generateCv(candidate, true, true);
        try (InputStream reportStream = report.getInputStream()) {
            IOUtils.copy(reportStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

    @PostMapping("submit")
    public Map<String, Object> submitRegistration() {
        Candidate candidate = this.candidateService.submitRegistration();
        return candidateStatusDto().build(candidate);
    }

    private DtoBuilder candidateNumberDto() {
        return new DtoBuilder()
                .add("candidateNumber")
                ;
    }

    private DtoBuilder candidateContactDto() {
        return new DtoBuilder()
                .add("user", userDto())
                .add("phone")
                .add("whatsapp")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("username")
                .add("email")
                .add("firstName")
                .add("lastName")
                ;
    }

    private DtoBuilder candidatePersonalDto() {
        return new DtoBuilder()
                .add("user", userDto())
                .add("candidateNumber")
                .add("gender")
                .add("dob")
                .add("country", countryDto())
                .add("city")
                .add("state")
                .add("yearOfArrival")
                .add("nationality", countryDto())
                .add("externalId")
                .add("unhcrRegistered")
                .add("unhcrNumber")
                .add("unhcrConsent")
                ;
    }

    private DtoBuilder candidateWithCandidateOccupationsDto() {
        return new DtoBuilder()
                .add("candidateOccupations", candidateOccupationDto())
                ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationDto())
                .add("yearsExperience")
                .add("migrationOccupation")
                ;
    }

    private DtoBuilder candidateEducationLevelDto() {
        return new DtoBuilder()
                .add("maxEducationLevel", educationLevelDto())
                ;
    }

    private DtoBuilder educationDto() {
        return new DtoBuilder()
                .add("id")
                .add("courseName")
                .add("educationType")
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("yearCompleted")
                .add("incomplete")
                .add("country", countryDto())
                .add("educationMajor", majorDto())
                ;
    }

    private DtoBuilder majorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder educationLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder candidateWithEducationDto() {
        return new DtoBuilder()
                .add("maxEducationLevel", educationLevelDto())
                .add("candidateEducations", educationDto())
                ;
    }

    private DtoBuilder candidateAdditionalInfoDto() {
        return new DtoBuilder()
                .add("id")
                .add("additionalInfo")
                .add("linkedInLink")
                ;
    }

    private DtoBuilder candidateSurveyDto() {
        return new DtoBuilder()
                .add("id")
                .add("surveyType", surveyTypeDto())
                .add("surveyComment")
                ;
    }

    private DtoBuilder surveyTypeDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder candidateWithJobExperiencesDto() {
        return new DtoBuilder()
                .add("candidateJobExperiences", jobExperienceDto())
                ;
    }

    private DtoBuilder jobExperienceDto() {
        return new DtoBuilder()
                .add("id")
                .add("country", countryDto())
                .add("candidateOccupation", candidateOccupationDto())
                .add("companyName")
                .add("role")
                .add("startDate")
                .add("endDate")
                .add("fullTime")
                .add("paid")
                .add("description")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder candidateWithCertificationsDto() {
        return new DtoBuilder()
                .add("candidateCertifications", certificationDto())
                ;
    }

    private DtoBuilder certificationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("institution")
                .add("dateCompleted")
                ;
    }

    private DtoBuilder candidateWithCandidateLanguagesDto() {
        return new DtoBuilder()
                .add("candidateLanguages", candidateLanguageDto())
                ;
    }

    private DtoBuilder candidateLanguageDto() {
        return new DtoBuilder()
                .add("id")
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

    private DtoBuilder candidateStatusDto() {
        return new DtoBuilder()
                .add("user", userDto())
                .add("status")
                .add("candidateMessage")
                ;
    }

    private DtoBuilder candidateProfileDto() {
        return new DtoBuilder()
                .add("user", userDto())
                /* CONTACT */
                .add("candidateNumber")
                .add("phone")
                .add("whatsapp")
                /* PERSONAL */
                .add("gender")
                .add("dob")
                .add("city")
                .add("state")
                .add("yearOfArrival")
                .add("nationality", countryDto())
                .add("country", countryDto())
                .add("externalId")
                .add("unhcrRegistered")
                .add("unhcrNumber")
                .add("unhcrConsent")
                /* OCCUPATIONS */
                .add("candidateOccupations", candidateOccupationDto())
                /* JOB EXPERIENCE */
                .add("candidateJobExperiences", jobExperienceDto())
                /* EDUCATIONS */
                .add("maxEducationLevel", educationLevelDto())
                .add("candidateEducations", educationDto())
                /* LANGUAGES */
                .add("candidateLanguages", candidateLanguageDto())
                /* CERTIFICATIONS */
                .add("candidateCertifications", certificationDto())
                /* ADDITIONAL INFO / SUBMIT */
                .add("additionalInfo")
                .add("candidateMessage")
                .add("surveyType", surveyTypeDto())
                .add("surveyComment")
                .add("linkedInLink")
                .add("taskAssignments", TaskDtoHelper.getTaskAssignmentDto())
                .add("candidateOpportunities", candidateOpportunityDto())
                ;
    }


    private DtoBuilder candidateOpportunityDto() {
        return new DtoBuilder()
            .add("id")
            .add("closingCommentsForCandidate")
            .add("jobOpp", jobDto())
            .add("lastModifiedDate")
            .add("name")
            .add("nextStep")
            .add("nextStepDueDate")
            .add("stage")
            ;
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

}
