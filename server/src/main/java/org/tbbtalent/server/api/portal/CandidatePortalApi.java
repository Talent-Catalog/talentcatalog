package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateAlternateContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateEmailRequest;
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
                .add("email")
                .add("phone")
                .add("whatsapp")
                ;
    }
}
