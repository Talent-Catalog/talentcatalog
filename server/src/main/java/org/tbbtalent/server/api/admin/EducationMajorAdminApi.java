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
import org.tbbtalent.server.model.db.EducationMajor;
import org.tbbtalent.server.request.education.major.CreateEducationMajorRequest;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;
import org.tbbtalent.server.request.education.major.UpdateEducationMajorRequest;
import org.tbbtalent.server.service.db.EducationMajorService;
import org.tbbtalent.server.util.dto.DtoBuilder;

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
