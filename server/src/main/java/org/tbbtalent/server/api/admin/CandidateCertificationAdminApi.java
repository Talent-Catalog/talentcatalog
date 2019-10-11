package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.CandidateCertification;
import org.tbbtalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tbbtalent.server.service.CandidateCertificationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-certification")
public class CandidateCertificationAdminApi {

    private final CandidateCertificationService candidateCertificationService;

    @Autowired
    public CandidateCertificationAdminApi(CandidateCertificationService candidateCertificationService) {
        this.candidateCertificationService = candidateCertificationService;
    }


    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateCertification> candidateCertifications = this.candidateCertificationService.list(id);
        return candidateCertificationDto().buildList(candidateCertifications);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@PathVariable("id") long candidateId,
                                      @RequestBody CreateCandidateCertificationRequest request) throws UsernameTakenException {
        CandidateCertification candidateCertification = this.candidateCertificationService.createCandidateCertificationAdmin(candidateId, request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateCertificationRequest request) {
        CandidateCertification candidateCertification = this.candidateCertificationService.updateCandidateCertificationAdmin(id, request);
        return candidateCertificationDto().build(candidateCertification);
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
