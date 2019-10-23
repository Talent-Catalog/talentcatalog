package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.Translation;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
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

    @PostMapping("countries")
    public Map<String, Object> search(@RequestBody SearchCountryRequest request) {
        Page<Country> countries = this.countryService.searchCountries(request);
        return translationDto().buildPage(countries);
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



    private DtoBuilder translationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                .add("translatedId")
                .add("translatedName")
                ;
    }

}
