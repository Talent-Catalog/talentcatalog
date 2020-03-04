package org.tbbtalent.server.service;

import org.tbbtalent.server.model.AbstractTranslatableDomainObject;
import org.tbbtalent.server.model.Translation;
import org.tbbtalent.server.request.translation.CreateTranslationRequest;
import org.tbbtalent.server.request.translation.UpdateTranslationRequest;

import java.util.List;
import java.util.Map;

public interface TranslationService {

    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,  String type);

    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items, String type, String language);

    Translation createTranslation(CreateTranslationRequest request);

    Translation updateTranslation(long id, UpdateTranslationRequest request);

    List<Translation> list();

    void updateTranslationFile(String language, Map translations);

    Map<String, Object> getTranslationFile(String language);
}
