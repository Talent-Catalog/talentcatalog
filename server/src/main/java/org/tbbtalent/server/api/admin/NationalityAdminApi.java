package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Nationality;
import org.tbbtalent.server.request.nationality.CreateNationalityRequest;
import org.tbbtalent.server.request.nationality.SearchNationalityRequest;
import org.tbbtalent.server.request.nationality.UpdateNationalityRequest;
import org.tbbtalent.server.service.NationalityService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/nationality")
public class NationalityAdminApi {

    private final NationalityService nationalityService;

    @Autowired
    public NationalityAdminApi(NationalityService nationalityService) {
        this.nationalityService = nationalityService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllNationalities() {
        List<Nationality> nationalities = nationalityService.listNationalities();
        return nationalityDto().buildList(nationalities);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchNationalityRequest request) {
        Page<Nationality> nationalities = this.nationalityService.searchNationalities(request);
        return nationalityDto().buildPage(nationalities);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Nationality nationality = this.nationalityService.getNationality(id);
        return nationalityDto().build(nationality);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateNationalityRequest request) throws EntityExistsException {
        Nationality nationality = this.nationalityService.createNationality(request);
        return nationalityDto().build(nationality);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateNationalityRequest request) throws EntityExistsException  {

        Nationality nationality = this.nationalityService.updateNationality(id, request);
        return nationalityDto().build(nationality);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.nationalityService.deleteNationality(id);
    }


    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
