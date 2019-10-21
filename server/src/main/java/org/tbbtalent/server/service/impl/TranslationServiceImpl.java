package org.tbbtalent.server.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.Translatable;
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

    public <T extends Translatable> void translate(List<T> items,
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

}
