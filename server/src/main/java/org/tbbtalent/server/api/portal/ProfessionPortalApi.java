package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.request.profession.CreateCandidateOccupationRequest;
import org.tbbtalent.server.service.CandidateOccupationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/profession")
public class ProfessionPortalApi {

    private final CandidateOccupationService candidateOccupationService;

    @Autowired
    public ProfessionPortalApi(CandidateOccupationService candidateOccupationService) {
        this.candidateOccupationService = candidateOccupationService;
    }

    @PostMapping()
    public Map<String, Object> createProfession(@Valid @RequestBody CreateCandidateOccupationRequest request) {
        CandidateOccupation candidateOccupation = candidateOccupationService.createCandidateOccupation(request);
        return professionDto().build(candidateOccupation);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteProfession(@PathVariable("id") Long id) {
        candidateOccupationService.deleteCandidateOccupation(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder professionDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationDto())
                .add("yearsExperience")
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
