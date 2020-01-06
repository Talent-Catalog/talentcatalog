package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.LanguageLevel;
import org.tbbtalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.UpdateLanguageLevelRequest;

import java.util.List;

public interface LanguageLevelService {

    List<LanguageLevel> listLanguageLevels();

    Page<LanguageLevel> searchLanguageLevels(SearchLanguageLevelRequest request);

    LanguageLevel getLanguageLevel(long id);

    LanguageLevel createLanguageLevel(CreateLanguageLevelRequest request) throws EntityExistsException;

    LanguageLevel updateLanguageLevel(long id, UpdateLanguageLevelRequest request) throws EntityExistsException ;

    boolean deleteLanguageLevel(long id) throws EntityReferencedException;

}
