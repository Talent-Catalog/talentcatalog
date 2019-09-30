package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.nationality.CreateNationalityRequest;
import org.tbbtalent.server.request.nationality.SearchNationalityRequest;
import org.tbbtalent.server.request.nationality.UpdateNationalityRequest;
import org.tbbtalent.server.request.user.SearchUserRequest;
import org.tbbtalent.server.service.NationalityService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

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
    public Map<String, Object> create(@RequestBody CreateNationalityRequest request) throws EntityExistsException {
        Nationality nationality = this.nationalityService.createNationality(request);
        return nationalityDto().build(nationality);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateNationalityRequest request) throws EntityExistsException  {
        Nationality nationality = this.nationalityService.updateNationality(id, request);
        return nationalityDto().build(nationality);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) {
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
