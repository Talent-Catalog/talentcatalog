package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.service.CountryService;
import org.tbbtalent.server.service.TranslationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/translation")
public class TranslationAdminApi {

    private final TranslationService translationService;
    private final CountryService countryService;

    @Autowired
    public TranslationAdminApi(TranslationService translationService, CountryService countryService) {
        this.translationService = translationService;
        this.countryService = countryService;
    }

//    @GetMapping()
//    public List<Map<String, Object>> listAllTranslations() {
//        List<Translation> translations = translationService.listTranslations();
//        return translationDto().buildList(translations);
//    }

    @PostMapping("countries/{systemLanguage}")
    public List<Map<String, Object>> search(@PathVariable("systemLanguage") String systemLanguage) {
        List<Country> countries = this.countryService.listCountries(systemLanguage);
        return countryDto().buildList(countries);
    }

//    @GetMapping("{id}")
//    public Map<String, Object> get(@PathVariable("id") long id) {
//        Translation translation = this.translationService.getTranslation(id);
//        return translationDto().build(translation);
//    }
//
//    @PostMapping
//    public Map<String, Object> create(@Valid @RequestBody CreateTranslationRequest request) throws EntityExistsException {
//        Translation translation = this.translationService.createTranslation(request);
//        return translationDto().build(translation);
//    }
//
//    @PutMapping("{id}")
//    public Map<String, Object> update(@PathVariable("id") long id,
//                                      @Valid @RequestBody UpdateTranslationRequest request) throws EntityExistsException  {
//
//        Translation translation = this.translationService.updateTranslation(id, request);
//        return translationDto().build(translation);
//    }
//
//    @DeleteMapping("{id}")
//    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
//        return this.translationService.deleteTranslation(id);
//    }



    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
