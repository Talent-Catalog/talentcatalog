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

    @GetMapping("profession")
    public Map<String, Object> getCandidateProfessions() {
        Candidate candidate = this.candidateService.getLoggedInCandidateLoadProfessions();
        return candidateWithProfessionsDto().build(candidate);
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

    private DtoBuilder candidateContactDto() {
        return new DtoBuilder()
                .add("email")
                ;
    }

    private DtoBuilder candidateAdditionalContactDto() {
        return new DtoBuilder()
                .add("email")
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
                .add("firstName")
                .add("lastName")
                .add("gender")
                .add("dob")
                ;
    }

    private DtoBuilder candidateWithProfessionsDto() {
        return new DtoBuilder()
                .add("professions", professionDto())
                ;
    }

    private DtoBuilder professionDto() {
        return new DtoBuilder()
                .add("id")
                .add("industry", industryDto())
                .add("yearsExperience")
                ;
    }

    private DtoBuilder candidateLocationDto() {
        return new DtoBuilder()
                .add("country")
                .add("city")
                .add("yearOfArrival")
                ;
    }

    private DtoBuilder candidateNationalityDto() {
        return new DtoBuilder()
                .add("nationality")
                .add("registeredWithUN")
                .add("registrationId")
                ;
    }

    private DtoBuilder candidateEducationLevelDto() {
        return new DtoBuilder()
                .add("educationLevel")
                ;
    }

    private DtoBuilder educationDto() {
        return new DtoBuilder()
                .add("id")
                .add("educationType")
                .add("countryId")
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("dateCompleted")
                ;
    }

    private DtoBuilder candidateWithEducationsDto() {
        return new DtoBuilder()
                .add("educations", educationDto())
                ;
    }

    private DtoBuilder industryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }
}
