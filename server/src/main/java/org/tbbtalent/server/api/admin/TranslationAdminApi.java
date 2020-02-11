package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.education.level.SearchEducationLevelRequest;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;
import org.tbbtalent.server.request.language.SearchLanguageRequest;
import org.tbbtalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tbbtalent.server.request.nationality.SearchNationalityRequest;
import org.tbbtalent.server.request.occupation.SearchOccupationRequest;
import org.tbbtalent.server.request.translation.CreateTranslationRequest;
import org.tbbtalent.server.request.translation.UpdateTranslationRequest;
import org.tbbtalent.server.service.*;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/translation")
public class TranslationAdminApi {

    private final TranslationService translationService;
    private final CountryService countryService;
    private final NationalityService nationalityService;
    private final LanguageService languageService;
    private final LanguageLevelService languagelLevelService;
    private final OccupationService occpuationService;
    private final EducationLevelService educationLevelService;
    private final EducationMajorService educationMajorService;

    @Autowired
    public TranslationAdminApi(TranslationService translationService, CountryService countryService, NationalityService nationalityService, LanguageService languageService, LanguageLevelService languagelLevelService, OccupationService occpuationService, EducationLevelService educationLevelService, EducationMajorService educationMajorService) {
        this.translationService = translationService;
        this.countryService = countryService;
        this.nationalityService = nationalityService;
        this.languageService = languageService;
        this.languagelLevelService = languagelLevelService;
        this.occpuationService = occpuationService;
        this.educationLevelService = educationLevelService;
        this.educationMajorService = educationMajorService;
    }

    @PostMapping("country")
    public Map<String, Object> search(@RequestBody SearchCountryRequest request) {
        Page<Country> countries = this.countryService.searchCountries(request);
        return translatedObjectDto().buildPage(countries);
    }

    @PostMapping("nationality")
    public Map<String, Object> searchNationality(@RequestBody SearchNationalityRequest request) {
        Page<Nationality> nationalities = this.nationalityService.searchNationalities(request);
        return translatedObjectDto().buildPage(nationalities);
    }

    @PostMapping("language")
    public Map<String, Object> searchLanguages(@RequestBody SearchLanguageRequest request) {
        Page<Language> languages = this.languageService.searchLanguages(request);
        return translatedObjectDto().buildPage(languages);
    }

    @PostMapping("language_level")
    public Map<String, Object> searchLanguageLevels(@RequestBody SearchLanguageLevelRequest request) {
        Page<LanguageLevel> languageLevels = this.languagelLevelService.searchLanguageLevels(request);
        return translatedObjectDto().buildPage(languageLevels);
    }

    @PostMapping("occupation")
    public Map<String, Object> searchOccupations(@RequestBody SearchOccupationRequest request) {
        Page<Occupation> occupations = this.occpuationService.searchOccupations(request);
        return translatedObjectDto().buildPage(occupations);
    }

    @PostMapping("education_level")
    public Map<String, Object> searchEducationLevels(@RequestBody SearchEducationLevelRequest request) {
        Page<EducationLevel> educationLevels = this.educationLevelService.searchEducationLevels(request);
        return translatedObjectDto().buildPage(educationLevels);
    }

    @PostMapping("education_major")
    public Map<String, Object> searchEducationMajors(@RequestBody SearchEducationMajorRequest request) {
        Page<EducationMajor> educationMajors = this.educationMajorService.searchEducationMajors(request);
        return translatedObjectDto().buildPage(educationMajors);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateTranslationRequest request) throws EntityExistsException {
        Translation translation = this.translationService.createTranslation(request);
        return translationDto().build(translation);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateTranslationRequest request) throws EntityExistsException {

        Translation translation = this.translationService.updateTranslation(id, request);
        return translationDto().build(translation);
    }

    @GetMapping("file/{language}")
    public Map<String, Object> getTranslationFile(@PathVariable("language") String language) {
        return this.translationService.getTranslationFile(language);
    }

    @PutMapping("file/{language}")
    public Map<String, Object> updateTranslationFile(
            @PathVariable("language") String language,
            @Valid @RequestBody Map translations) {
        this.translationService.updateTranslationFile(language, translations);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        return result;
    }

    private DtoBuilder translatedObjectDto() {
        return new DtoBuilder(true)
                .add("id")
                .add("name")
                .add("status")
                .add("translatedId")
                .add("translatedName")
                ;
    }

    private DtoBuilder translationDto() {
        return new DtoBuilder(true)
                .add("objectId")
                .add("objectType")
                .add("language")
                .add("value")
                ;
    }

}
