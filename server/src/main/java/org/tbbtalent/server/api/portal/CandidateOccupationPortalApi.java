package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.request.occupation.CreateCandidateOccupationRequest;
import org.tbbtalent.server.service.CandidateOccupationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/candidate-occupation")
public class CandidateOccupationPortalApi {

    private final CandidateOccupationService candidateOccupationService;

    @Autowired
    public CandidateOccupationPortalApi(CandidateOccupationService candidateOccupationService) {
        this.candidateOccupationService = candidateOccupationService;
    }

    @GetMapping("list")
    public List<Map<String, Object>> listMyOccupations() {
        List<CandidateOccupation> candidateOccupations = candidateOccupationService.listMyOccupations();
        return candidateOccupationDto().buildList(candidateOccupations);
    }

    @PostMapping()
    public Map<String, Object> createCandidateOccupation(@Valid @RequestBody CreateCandidateOccupationRequest request) {
        CandidateOccupation candidateOccupation = candidateOccupationService.createCandidateOccupation(request);
        return candidateOccupationDto().build(candidateOccupation);
    }


    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateOccupation(@PathVariable("id") Long id) {
        candidateOccupationService.deleteCandidateOccupation(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateOccupationDto() {
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
