package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.request.education.major.CreateEducationMajorRequest;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;
import org.tbbtalent.server.request.education.major.UpdateEducationMajorRequest;
import org.tbbtalent.server.service.EducationMajorService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/education-major")
public class EducationMajorAdminApi {

    private final EducationMajorService educationMajorService;

    @Autowired
    public EducationMajorAdminApi(EducationMajorService educationMajorService) {
        this.educationMajorService = educationMajorService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllEducationMajors() {
        List<EducationMajor> educationMajors = educationMajorService.listActiveEducationMajors();
        return educationMajorDto().buildList(educationMajors);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchEducationMajorRequest request) {
        Page<EducationMajor> nationalities = this.educationMajorService.searchEducationMajors(request);
        return educationMajorDto().buildPage(nationalities);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        EducationMajor educationMajor = this.educationMajorService.getEducationMajor(id);
        return educationMajorDto().build(educationMajor);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateEducationMajorRequest request) throws EntityExistsException {
        EducationMajor educationMajor = this.educationMajorService.createEducationMajor(request);
        return educationMajorDto().build(educationMajor);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateEducationMajorRequest request) throws EntityExistsException  {

        EducationMajor educationMajor = this.educationMajorService.updateEducationMajor(id, request);
        return educationMajorDto().build(educationMajor);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.educationMajorService.deleteEducationMajor(id);
    }


    private DtoBuilder educationMajorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
