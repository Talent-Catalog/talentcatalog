package org.tbbtalent.server.api.portal;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.db.CandidateCertification;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.service.db.CandidateCertificationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate-certification")
public class CandidateCertificationPortalApi {

    private final CandidateCertificationService candidateCertificationService;

    @Autowired
    public CandidateCertificationPortalApi(CandidateCertificationService candidateCertificationService) {
        this.candidateCertificationService = candidateCertificationService;
    }

    @PostMapping()
    public Map<String, Object> createCandidateCertification(@Valid @RequestBody CreateCandidateCertificationRequest request) {
        CandidateCertification candidateCertification = candidateCertificationService.createCandidateCertification(request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateCertificationRequest request) {
        CandidateCertification candidateCertification = this.candidateCertificationService.updateCandidateCertificationAdmin(id, request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateCertification(@PathVariable("id") Long id) {
        candidateCertificationService.deleteCandidateCertification(id);
        return ResponseEntity.ok().build();
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
