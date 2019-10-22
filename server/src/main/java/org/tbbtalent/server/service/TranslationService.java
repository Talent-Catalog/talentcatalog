package org.tbbtalent.server.service;

import org.tbbtalent.server.model.AbstractTranslatableDomainObject;

import java.util.List;

public interface TranslationService {

//    List search(SearchTranslationRequest request);
    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items, String type);
    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items, String type, String selectedLanguage);
//    List<Country> translate(List<Country> countries);
//    List<Country> translate(String selectedLanguage, List<Country> countries);

}
