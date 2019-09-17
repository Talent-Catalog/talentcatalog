package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.request.language.CreateLanguageRequest;

public interface LanguageService {

    Language createLanguage(CreateLanguageRequest request);

}
