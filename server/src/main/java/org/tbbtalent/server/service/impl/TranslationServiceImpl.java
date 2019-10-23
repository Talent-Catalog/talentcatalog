package org.tbbtalent.server.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.AbstractTranslatableDomainObject;
import org.tbbtalent.server.model.Translation;
import org.tbbtalent.server.repository.TranslationRepository;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.TranslationService;

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

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,
                                                                             String type) {
        String selectedLanguage = userContext.getUserLanguage();
        translate(items, type, selectedLanguage);
    }

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,
                                                                             String type, String selectedLanguage) {
        // if the selected language is english, no need to load translations at all, just return original data
        if ("en".equals(selectedLanguage)) {
            return;
        }

        if (CollectionUtils.isNotEmpty(items)) {
            List<Long> itemIds = items.stream().map(c -> (Long) c.getId()).collect(Collectors.toList());
            List<Translation> translations = translationRepository.findByIdsTypeLanguage(itemIds, type, selectedLanguage);
            if (CollectionUtils.isNotEmpty(translations)) {
                Map<Long, Translation> translationsById = translations.stream().collect(Collectors.toMap(Translation::getObjectId, Function.identity()));
                items.forEach(c -> {
                    Translation translation = translationsById.get(c.getId());
                    if (translation != null) {
                        c.setTranslatedId(translation.getId());
                        c.setTranslatedName(translation.getValue());
                    }
                });
            }
        }
    }
}


