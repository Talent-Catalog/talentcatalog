package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateEducationLevelRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/candidate")
public class CandidatePortalApi {

    private final CandidateService candidateService;

    @Autowired
    public CandidatePortalApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping("contact")
    public Map<String, Object> getCandidateEmail() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateContactDto().build(candidate);
    }

    @PostMapping("contact")
    public Map<String, Object> updateCandidateEmail(@Valid @RequestBody UpdateCandidateContactRequest request) {
        Candidate candidate = this.candidateService.updateContact(request);
        return candidateContactDto().build(candidate);
    }

    @GetMapping("personal")
    public Map<String, Object> getCandidatePersonal() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidatePersonalDto().build(candidate);
    }

    @PostMapping("personal")
    public Map<String, Object> updateCandidatePersonal(@Valid @RequestBody UpdateCandidatePersonalRequest request) {
        Candidate candidate = this.candidateService.updatePersonal(request);
        return candidatePersonalDto().build(candidate);
    }

    @GetMapping("occupation")
    public Map<String, Object> getCandidateCandidateOccupations() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadCandidateOccupations();
        return candidateWithCandidateOccupationsDto().build(candidate);
    }

    @GetMapping("education")
    public Map<String, Object> getCandidateEducation() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadEducations();
        return candidateWithEducationDto().build(candidate);
    }

    @PostMapping("education")
    public Map<String, Object> updateCandidateEducationLevel(@Valid @RequestBody UpdateCandidateEducationLevelRequest request) {
        Candidate candidate = this.candidateService.updateEducationLevel(request);
        return candidateEducationLevelDto().build(candidate);
    }

    @GetMapping("additional-info")
    public Map<String, Object> getCandidateAdditionalInfo() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateAdditionalInfoDto().build(candidate);
    }

    @PostMapping("additional-info")
    public Map<String, Object> updateCandidateAdditionalInfo(@Valid @RequestBody UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = this.candidateService.updateAdditionalInfo(request);
        return candidateAdditionalInfoDto().build(candidate);
    }

    @GetMapping("job-experiences")
    public Map<String, Object> getCandidateJobExperiences() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadJobExperiences();
        return candidateWithJobExperiencesDto().build(candidate);
    }

    @GetMapping("certifications")
    public Map<String, Object> getCandidateCertifications() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadCertifications();
        return candidateWithCertificationsDto().build(candidate);
    }

    @GetMapping("languages")
    public Map<String, Object> getCandidateLanguages() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadCandidateLanguages();
        return candidateWithCandidateLanguagesDto().build(candidate);
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
                .add("gender")
                .add("dob")
                .add("country", countryDto())
                .add("city")
                .add("yearOfArrival")
                .add("nationality", nationalityDto())
                .add("unRegistered")
                .add("unRegistrationNumber")
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
                ;
    }

    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
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
                .add("educationType")
                .add("country", countryDto())
                .add("courseName")
                .add("dateCompleted")
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
                .add("candidateEducation", educationDto())
                ;
    }

    private DtoBuilder candidateAdditionalInfoDto() {
        return new DtoBuilder()
                .add("id")
                .add("additionalInfo")
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
}
