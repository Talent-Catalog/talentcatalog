package org.tbbtalent.server.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.AbstractTranslatableDomainObject;
import org.tbbtalent.server.model.Translation;
import org.tbbtalent.server.repository.TranslationRepository;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.TranslationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TranslationServiceImpl implements TranslationService {

    private final TranslationRepository translationRepository;
    private final UserContext userContext;

    @Autowired
    public TranslationServiceImpl(TranslationRepository translationRepository,
                                  UserContext userContext) {
        this.userContext = userContext;
        this.translationRepository = translationRepository;
    }
//    @Override
//    public List search(SearchTranslationRequest request) {
//        List translations = new ArrayList<>();
//        if(request.getObjectType() == "country"){
//            List<Country> countries = countryService.listCountries();
//            translations = translate(request.getSystemLanguage(), countries);
//            return translations;
//        }else{
//
//        }
//        return translations;
//    }

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,
                                                                             String type) {
        // if the selected language is english, no need to load translations at all, just return original data
        String selectedLanguage = userContext.getUserLanguage();
        if ("en".equals(selectedLanguage)) {
            return;
        }

        if (CollectionUtils.isNotEmpty(items)) {
            List<Long> itemIds = items.stream().map(c -> (Long)c.getId()).collect(Collectors.toList());
            List<Translation> translations = translationRepository.findByIdsTypeLanguage(itemIds, type, selectedLanguage);
            if (CollectionUtils.isNotEmpty(translations)) {
                Map<Long, String> translationsById = translations.stream().collect(Collectors.toMap(t -> t.getObjectId(), t -> t.getValue()));
                items.forEach(c -> {
                    String translation = translationsById.get(c.getId());
                    if (StringUtils.isNotBlank(translation)) {
                        c.setTranslatedName(translation);
                    }
                });
            }
        }
    }


//    // GETTING LANGUAGE FROM USER
//    @Override
//    public List<Country> translate(List<Country> countries) {
//        // TODO: if the selected language is english, no need to load translations at all, just return original data
//        String selectedLanguage = userContext.getUserLanguage();
//        return translate(selectedLanguage, countries);
//    }
//
//    // GETTING LANGUAGE FROM ADMIN TRANSLATION SETTINGS
//    @Override
//    public List<Country> translate(String selectedLanguage, List<Country> countries) {
//        // TODO: if the selected language is english, no need to load translations at all, just return original data
//        List<Country> translatedCountries = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(countries)) {
//            List<Long> countryIds = countries.stream().map(c -> c.getId()).collect(Collectors.toList());
//            // copying the original object should avoid hibernate accidentally updating an object because the transaction boundaries were not right
//            translatedCountries = countries.stream().map(c -> new Country(c)).collect(Collectors.toList());
//
//            List<Translation> translations = translationRepository.findByIdsTypeLanguage(countryIds, "countries", selectedLanguage);
//            if (CollectionUtils.isNotEmpty(translations)) {
//                Map<Long, String> translationsById = translations.stream().collect(Collectors.toMap(t -> t.getObjectId(), t -> t.getValue()));
//                translatedCountries.forEach(c -> {
//                    String translation = translationsById.get(c.getId());
//                    if (StringUtils.isNotBlank(translation)) {
//                        c.setName(translation);
//                    }
//                });
//            }
//        }
//        return translatedCountries;
//    }

}


//    @Override
//    public List<Country> translate(List<Country> countries) {
//
//        // TODO: if the selected language is english, no need to load translations at all, just return original data
//
//        List<Country> translatedCountries = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(countries)) {
//            List<Long> countryIds = countries.stream().map(c -> c.getId()).collect(Collectors.toList());
//            // copying the original object should avoid hibernate accidentally updating an object because the transaction boundaries were not right
//            translatedCountries = countries.stream().map(c -> new Country(c)).collect(Collectors.toList());
//
//            String selectedLanguage = userContext.getUserLanguage();
//            List<Translation> translations = translationRepository.findByIdsTypeLanguage(countryIds, "country", selectedLanguage);
//            if (CollectionUtils.isNotEmpty(translations)) {
//                Map<Long, String> translationsById = translations.stream().collect(Collectors.toMap(t -> t.getObjectId(), t -> t.getValue()));
//                translatedCountries.forEach(c -> {
//                    String translation = translationsById.get(c.getId());
//                    if (StringUtils.isNotBlank(translation)) {
//                        c.setName(translation);
//                    }
//                });
//            }
//        }
//        return translatedCountries;
//    }

