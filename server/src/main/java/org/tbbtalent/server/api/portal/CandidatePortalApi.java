package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateAlternateContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateEmailRequest;
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
                .add("industry", indutryDto())
                .add("yearsExperience")
                ;
    }

    private DtoBuilder indutryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }
}
