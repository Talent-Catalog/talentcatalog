package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Country;

import java.util.List;

public interface TranslationService {

//    List search(SearchTranslationRequest request);
    List<Country> translate(List<Country> countries);
    List<Country> translate(String selectedLanguage, List<Country> countries);

}
