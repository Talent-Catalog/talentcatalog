package org.tbbtalent.server.service;

import java.util.List;
import java.util.Map;

import org.tbbtalent.server.model.db.AbstractTranslatableDomainObject;
import org.tbbtalent.server.model.db.Translation;
import org.tbbtalent.server.request.translation.CreateTranslationRequest;
import org.tbbtalent.server.request.translation.UpdateTranslationRequest;

public interface TranslationService {

    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,  String type);

    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items, String type, String language);

    Translation createTranslation(CreateTranslationRequest request);

    Translation updateTranslation(long id, UpdateTranslationRequest request);

    List<Translation> list();

    void updateTranslationFile(String language, Map translations);

    Map<String, Object> getTranslationFile(String language);
}
