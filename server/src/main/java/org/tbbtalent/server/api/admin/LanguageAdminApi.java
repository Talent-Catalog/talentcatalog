package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.service.LanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/language")
public class LanguageAdminApi {

    private final LanguageService languageService;

    @Autowired
    public LanguageAdminApi(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguages() {
        List<Language> languages = languageService.listLanguages();
        return languageDto().buildList(languages);
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
