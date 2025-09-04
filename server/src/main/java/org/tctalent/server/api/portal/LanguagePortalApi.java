/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.api.portal;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.dto.SystemLanguageDtoBuilder;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.response.DatePickerNames;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.TranslationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping({"/api/portal/language", "/api/admin/translate"})
public class LanguagePortalApi {

    private final LanguageService languageService;
    private final TranslationService translationService;
    private final DtoBuilder systemLanguageDtoBuilder = new SystemLanguageDtoBuilder();


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

    @GetMapping("/datepickernames/{lang}")
    public DatePickerNames getDatePickerNames(@PathVariable("lang") String lang) {
        return languageService.getDatePickerNames(lang);
    }

    @GetMapping(value = "system")
    public List<Map<String, Object>> getSystemLanguages() {
        List<SystemLanguage> languages = languageService.listSystemLanguages();
        return systemLanguageDtoBuilder.buildList(languages);
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

}
