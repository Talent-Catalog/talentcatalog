package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.LanguageLevel;
import org.tbbtalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.UpdateLanguageLevelRequest;
import org.tbbtalent.server.service.LanguageLevelService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/language-level")
public class LanguageLevelAdminApi {

    private final LanguageLevelService languageLevelService;

    @Autowired
    public LanguageLevelAdminApi(LanguageLevelService languageLevelService) {
        this.languageLevelService = languageLevelService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguages() {
        List<LanguageLevel> languageLevels = languageLevelService.listLanguageLevels();
        return languageLevelDto().buildList(languageLevels);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchLanguageLevelRequest request) {
        Page<LanguageLevel> languages = this.languageLevelService.searchLanguageLevels(request);
        return languageLevelDto().buildPage(languages);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        LanguageLevel languageLevel = this.languageLevelService.getLanguageLevel(id);
        return languageLevelDto().build(languageLevel);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateLanguageLevelRequest request) throws EntityExistsException {
        LanguageLevel languageLevel = this.languageLevelService.createLanguageLevel(request);
        return languageLevelDto().build(languageLevel);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateLanguageLevelRequest request) throws EntityExistsException  {

        LanguageLevel languageLevel = this.languageLevelService.updateLanguageLevel(id, request);
        return languageLevelDto().build(languageLevel);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.languageLevelService.deleteLanguageLevel(id);
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                .add("status")
                ;
    }

}
