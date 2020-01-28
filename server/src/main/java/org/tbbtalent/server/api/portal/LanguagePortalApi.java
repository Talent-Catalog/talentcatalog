package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.model.SystemLanguage;
import org.tbbtalent.server.service.LanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/language")
public class LanguagePortalApi {

    private final LanguageService languageService;

    @Autowired
    public LanguagePortalApi(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguages() {
        List<Language> languages = languageService.listLanguages();
        return languageDto().buildList(languages);
    }

    @GetMapping("{language}")
    public Map<String, Object> getLanguage(@PathVariable("language") String languageName) {
        Language language = languageService.getLanguage(languageName);
        return languageDto().build(language);
    }
    
    @GetMapping(value = "system")
    public List<Map<String, Object>> getSystemLanguages() {
        List<SystemLanguage> languages = languageService.listSystemLanguages();
        return systemLanguageDto().buildList(languages);
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }
    
    private DtoBuilder systemLanguageDto() {
        return new DtoBuilder()
                .add("id")
                .add("language")
                .add("label")
                ;
    }

}
