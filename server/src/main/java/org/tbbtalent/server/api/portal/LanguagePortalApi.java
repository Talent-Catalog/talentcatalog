package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.request.language.CreateLanguageRequest;
import org.tbbtalent.server.service.LanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/language")
public class LanguagePortalApi {

    private final LanguageService languageService;

    @Autowired
    public LanguagePortalApi(LanguageService languageService) {
        this.languageService = languageService;
    }

    @PostMapping()
    public Map<String, Object> createLanguage(@Valid @RequestBody CreateLanguageRequest request) {
        Language language = languageService.createLanguage(request);
        return languageDto().build(language);
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("readWrite")
                .add("speak")
                ;
    }


}
