package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.candidate.*;
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

    @GetMapping("contact/email")
    public Map<String, Object> getCandidateEmail() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateContactDto().build(candidate);
    }

    @PostMapping("contact/email")
    public Map<String, Object> updateCandidateEmail(@Valid @RequestBody UpdateCandidateEmailRequest request) {
        Candidate candidate = this.candidateService.updateEmail(request);
        return candidateContactDto().build(candidate);
    }

    @GetMapping("contact/alternate")
    public Map<String, Object> getCandidateAlternateContact() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateAlternateContactDto().build(candidate);
    }

    @PostMapping("contact/alternate")
    public Map<String, Object> updateCandidateAlternateContact(@Valid @RequestBody UpdateCandidateAlternateContactRequest request) {
        Candidate candidate = this.candidateService.updateAlternateContacts(request);
        return candidateAlternateContactDto().build(candidate);
    }

    @GetMapping("contact/additional")
    public Map<String, Object> getCandidateAdditionalContact() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateAdditionalContactDto().build(candidate);
    }

    @PostMapping("contact/additional")
    public Map<String, Object> updateCandidateAdditionalContact(@Valid @RequestBody UpdateCandidateAdditionalContactRequest request) {
        Candidate candidate = this.candidateService.updateAdditionalContacts(request);
        return candidateAlternateContactDto().build(candidate);
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

    @GetMapping("candidateOccupation")
    public Map<String, Object> getCandidateCandidateOccupations() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadCandidateOccupations();
        return candidateWithCandidateOccupationsDto().build(candidate);
    }

    @GetMapping("location")
    public Map<String, Object> getCandidateLocation() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateLocationDto().build(candidate);
    }

    @PostMapping("location")
    public Map<String, Object> updateCandidateLocation(@Valid @RequestBody UpdateCandidateLocationRequest request) {
        Candidate candidate = this.candidateService.updateLocation(request);
        return candidateLocationDto().build(candidate);
    }

    @GetMapping("nationality")
    public Map<String, Object> getCandidateNationality() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateNationalityDto().build(candidate);
    }

    @PostMapping("nationality")
    public Map<String, Object> updateCandidateNationality(@Valid @RequestBody UpdateCandidateNationalityRequest request) {
        Candidate candidate = this.candidateService.updateNationality(request);
        return candidateNationalityDto().build(candidate);
    }

    @GetMapping("education")
    public Map<String, Object> getCandidateEducationLevel() {
        Candidate candidate = this.candidateService.getLoggedInCandidate();
        return candidateEducationLevelDto().build(candidate);
    }

    @PostMapping("education")
    public Map<String, Object> updateCandidateEducationLevel(@Valid @RequestBody UpdateCandidateEducationLevelRequest request) {
        Candidate candidate = this.candidateService.updateEducationLevel(request);
        return candidateEducationLevelDto().build(candidate);
    }

    @GetMapping("educations")
    public Map<String, Object> getCandidateEducations() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadEducations();
        return candidateWithEducationsDto().build(candidate);
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

    private DtoBuilder candidateAdditionalContactDto() {
        return new DtoBuilder()
                .add("user", userDto())
                .add("phone")
                .add("whatsapp")
                ;
    }

    private DtoBuilder candidateAlternateContactDto() {
        return new DtoBuilder()
                .add("phone")
                .add("whatsapp")
                ;
    }

    private DtoBuilder candidatePersonalDto() {
        return new DtoBuilder()
                .add("user", userDto())
                .add("gender")
                .add("dob")
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

    private DtoBuilder candidateLocationDto() {
        return new DtoBuilder()
                .add("country", countryDto())
                .add("city")
                .add("yearOfArrival")
                ;
    }

    private DtoBuilder candidateNationalityDto() {
        return new DtoBuilder()
                .add("nationality", nationalityDto())
                .add("unRegistered")
                .add("unRegistrationNumber")
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

    private DtoBuilder candidateWithEducationsDto() {
        return new DtoBuilder()
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
                .add("level")
                ;
    }
}
