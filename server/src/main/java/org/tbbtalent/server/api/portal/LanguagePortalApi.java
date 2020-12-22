package org.tbbtalent.server.api.portal;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.SystemLanguage;
import org.tbbtalent.server.service.db.LanguageService;
import org.tbbtalent.server.service.db.TranslationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/language")
public class LanguagePortalApi {

    private final LanguageService languageService;
    private final TranslationService translationService;


    @Autowired
    public LanguagePortalApi(LanguageService languageService, TranslationService translationService) {
        this.languageService = languageService;
        this.translationService = translationService;
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

    @GetMapping("translations/file/{language}")
    public Map<String, Object> getTranslationFile(@PathVariable("language") String language) {
        return this.translationService.getTranslationFile(language);
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
