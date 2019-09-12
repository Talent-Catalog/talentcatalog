package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Profession;
import org.tbbtalent.server.request.profession.CreateProfessionRequest;
import org.tbbtalent.server.service.ProfessionService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/profession")
public class ProfessionPortalApi {

    private final ProfessionService professionService;

    @Autowired
    public ProfessionPortalApi(ProfessionService professionService) {
        this.professionService = professionService;
    }

    @PostMapping()
    public Map<String, Object> createProfession(@Valid @RequestBody CreateProfessionRequest request) {
        Profession profession = professionService.createProfession(request);
        return professionDto().build(profession);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteProfession(@PathVariable("id") Long id) {
        professionService.deleteProfession(id);
        return ResponseEntity.ok().build();
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
