package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.model.SystemLanguage;
import org.tbbtalent.server.request.language.CreateLanguageRequest;
import org.tbbtalent.server.request.language.SearchLanguageRequest;
import org.tbbtalent.server.request.language.UpdateLanguageRequest;

import java.util.List;

public interface LanguageService {

    List<Language> listLanguages();

    Page<Language> searchLanguages(SearchLanguageRequest request);

    Language getLanguage(long id);

    Language createLanguage(CreateLanguageRequest request) throws EntityExistsException;

    Language updateLanguage(long id, UpdateLanguageRequest request) throws EntityExistsException ;

    boolean deleteLanguage(long id) throws EntityReferencedException;

    List<SystemLanguage> listSystemLanguages();

}
