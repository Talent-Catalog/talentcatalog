package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.request.occupation.CreateOccupationRequest;
import org.tbbtalent.server.request.occupation.SearchOccupationRequest;
import org.tbbtalent.server.request.occupation.UpdateOccupationRequest;
import org.tbbtalent.server.service.OccupationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/occupation")
public class OccupationAdminApi {

    private final OccupationService occupationService;

    @Autowired
    public OccupationAdminApi(OccupationService occupationService) {
        this.occupationService = occupationService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllOccupations() {
        List<Occupation> occupations = occupationService.listOccupations();
        return occupationDto().buildList(occupations);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchOccupationRequest request) {
        Page<Occupation> occupations = this.occupationService.searchOccupations(request);
        return occupationDto().buildPage(occupations);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Occupation occupation = this.occupationService.getOccupation(id);
        return occupationDto().build(occupation);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateOccupationRequest request) throws EntityExistsException {
        Occupation occupation = this.occupationService.createOccupation(request);
        return occupationDto().build(occupation);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateOccupationRequest request) throws EntityExistsException  {

        Occupation occupation = this.occupationService.updateOccupation(id, request);
        return occupationDto().build(occupation);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.occupationService.deleteOccupation(id);
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
