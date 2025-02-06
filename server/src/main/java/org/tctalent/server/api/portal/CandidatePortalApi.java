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

package org.tctalent.server.api.portal;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TaskDtoHelper;
import org.tctalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tctalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tctalent.server.request.candidate.UpdateCandidateEducationRequest;
import org.tctalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tctalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate")
public class CandidatePortalApi {

    private final CandidateService candidateService;
    private final CountryService countryService;
    private final OccupationService occupationService;

    @Autowired
    public CandidatePortalApi(OccupationService occupationService, CandidateService candidateService,
        CountryService countryService) {
        this.occupationService = occupationService;
        this.candidateService = candidateService;
        this.countryService = countryService;
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

    @GetMapping("exams")
    public Map<String, Object> getCandidateCandidateExams() {
        Candidate candidate = this.candidateService
            .getLoggedInCandidateLoadCandidateExams()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateWithCandidateExamsDto().build(candidate);
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

    @GetMapping("destinations")
    public Map<String, Object> getCandidateDestinations() {
        Candidate candidate = this.candidateService
            .getLoggedInCandidateLoadDestinations()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        return candidateWithDestinationsDto().build(candidate);
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
                .add("publicId")
                ;
    }

    private DtoBuilder candidateContactDto() {
        return new DtoBuilder()
                .add("user", userDto())
                .add("phone")
                .add("whatsapp")
                /* RELOCATED ADDRESS */
                .add("relocatedAddress")
                .add("relocatedCity")
                .add("relocatedState")
                .add("relocatedCountry", countryService.selectBuilder())
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
                .add("publicId")
                .add("gender")
                .add("dob")
                .add("country", countryService.selectBuilder())
                .add("city")
                .add("state")
                .add("yearOfArrival")
                .add("nationality", countryService.selectBuilder())
                .add("candidateCitizenships", candidateCitizenshipDto())
                .add("externalId")
                .add("unhcrRegistered")
                .add("unhcrNumber")
                .add("unhcrConsent")
                ;
    }

    private DtoBuilder candidateCitizenshipDto() {
        return new DtoBuilder()
                .add("nationality", countryService.selectBuilder())
                ;
    }

    private DtoBuilder candidateWithCandidateOccupationsDto() {
        return new DtoBuilder()
                .add("candidateOccupations", candidateOccupationDto())
                ;
    }

    private DtoBuilder candidateWithCandidateExamsDto() {
        return new DtoBuilder()
            .add("candidateExams", candidateExamDto())
            ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationService.selectBuilder())
                .add("yearsExperience")
                .add("migrationOccupation")
                ;
    }

    private DtoBuilder candidateExamDto() {
        return new DtoBuilder()
            .add("id")
            .add("exam")
            .add("otherExam")
            .add("score")
            .add("year")
            .add("notes");
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
                .add("country", countryService.selectBuilder())
                .add("educationMajor", majorDto())
                ;
    }

    private DtoBuilder majorDto() {
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
                .add("allNotifications")
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
                .add("country", countryService.selectBuilder())
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

    private DtoBuilder candidateWithDestinationsDto() {
        return new DtoBuilder()
                .add("id")
                .add("candidateDestinations", destinationsDto())
                ;
    }

    private DtoBuilder destinationsDto() {
        return new DtoBuilder()
                .add("id")
                .add("country", countryService.selectBuilder())
                .add("interest")
                .add("notes")
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
                .add("id")
                .add("user", userDto())
                /* CONTACT */
                .add("candidateNumber")
                .add("publicId")
                .add("phone")
                .add("whatsapp")
                /* STATUS */
                .add("status")
                /* PERSONAL */
                .add("gender")
                .add("dob")
                .add("city")
                .add("state")
                .add("yearOfArrival")
                .add("nationality", countryService.selectBuilder())
                .add("country", countryService.selectBuilder())
                .add("externalId")
                .add("unhcrRegistered")
                .add("unhcrNumber")
                .add("unhcrConsent")
                /* RELOCATED ADDRESS */
                .add("relocatedAddress")
                .add("relocatedCity")
                .add("relocatedState")
                .add("relocatedCountry", countryService.selectBuilder())
                /* OCCUPATIONS */
                .add("candidateOccupations", candidateOccupationDto())
                /* JOB EXPERIENCE */
                .add("candidateJobExperiences", jobExperienceDto())
                /* EDUCATIONS */
                .add("maxEducationLevel", educationLevelDto())
                .add("candidateEducations", educationDto())
                /* LANGUAGES */
                .add("candidateLanguages", candidateLanguageDto())
                .add("candidateExams", candidateExamDto())
                /* CERTIFICATIONS */
                .add("candidateCertifications", certificationDto())
                /* ADDITIONAL INFO / SUBMIT */
                .add("additionalInfo")
                .add("candidateMessage")
                .add("surveyType", surveyTypeDto())
                .add("surveyComment")
                .add("linkedInLink")
                .add("allNotifications")
                .add("taskAssignments", TaskDtoHelper.getTaskAssignmentDto())
                .add("candidateOpportunities", candidateOpportunityDto())
                .add("candidateDestinations", destinationsDto())
                ;
    }


    private DtoBuilder candidateOpportunityDto() {
        return new DtoBuilder()
            .add("id")
            .add("candidate", shortCandidateDto())
            .add("closed")
            .add("closingCommentsForCandidate")
            .add("jobOpp", jobDto())
            .add("name")
            .add("nextStep")
            .add("nextStepDueDate")
            .add("lastActiveStage")
            .add("stage")
            .add("createdBy", userDto())
            .add("createdDate")
            .add("updatedBy", userDto())
            .add("updatedDate")
            .add("fileOfferLink")
            .add("fileOfferName")
            ;
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("submissionList", savedListDto())
            ;
    }

    private DtoBuilder savedListDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("fileInterviewGuidanceLink")
            .add("fileInterviewGuidanceName")
            ;
    }

    private DtoBuilder shortCandidateDto() {
        return new DtoBuilder()
            .add("id")
            .add("candidateNumber")
            .add("publicId")
            ;
    }


}
