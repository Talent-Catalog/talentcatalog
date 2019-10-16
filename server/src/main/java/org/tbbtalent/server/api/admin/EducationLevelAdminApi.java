package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.request.education.level.CreateEducationLevelRequest;
import org.tbbtalent.server.request.education.level.SearchEducationLevelRequest;
import org.tbbtalent.server.request.education.level.UpdateEducationLevelRequest;
import org.tbbtalent.server.service.EducationLevelService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/education-level")
public class EducationLevelAdminApi {

    private final EducationLevelService educationLevelService;

    @Autowired
    public EducationLevelAdminApi(EducationLevelService educationLevelService) {
        this.educationLevelService = educationLevelService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguages() {
        List<EducationLevel> educationLevels = educationLevelService.listEducationLevels();
        return educationLevelDto().buildList(educationLevels);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchEducationLevelRequest request) {
        Page<EducationLevel> languages = this.educationLevelService.searchEducationLevels(request);
        return educationLevelDto().buildPage(languages);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        EducationLevel educationLevel = this.educationLevelService.getEducationLevel(id);
        return educationLevelDto().build(educationLevel);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateEducationLevelRequest request) throws EntityExistsException {
        EducationLevel educationLevel = this.educationLevelService.createEducationLevel(request);
        return educationLevelDto().build(educationLevel);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateEducationLevelRequest request) throws EntityExistsException  {

        EducationLevel educationLevel = this.educationLevelService.updateEducationLevel(id, request);
        return educationLevelDto().build(educationLevel);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.educationLevelService.deleteEducationLevel(id);
    }

    private DtoBuilder educationLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                .add("level")
                ;
    }

}
