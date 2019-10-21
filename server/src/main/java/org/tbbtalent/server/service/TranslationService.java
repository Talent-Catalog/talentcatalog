package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.request.translation.SearchTranslationRequest;

import java.util.List;

public interface TranslationService {

    List search(SearchTranslationRequest request);
    List<Country> translate(List<Country> countries);
    List<Country> translate(String selectedLanguage, List<Country> countries);

}
