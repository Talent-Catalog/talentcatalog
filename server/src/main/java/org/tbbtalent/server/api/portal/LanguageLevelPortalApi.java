package org.tbbtalent.server.api.portal;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.LanguageLevel;
import org.tbbtalent.server.service.db.LanguageLevelService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/language-level")
public class LanguageLevelPortalApi {

    private final LanguageLevelService languageLevelService;

    @Autowired
    public LanguageLevelPortalApi(LanguageLevelService languageLevelService) {
        this.languageLevelService = languageLevelService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguageLevels() {
        List<LanguageLevel> languageLevels = languageLevelService.listLanguageLevels();
        return languageLevelDto().buildList(languageLevels);
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                ;
    }

}
