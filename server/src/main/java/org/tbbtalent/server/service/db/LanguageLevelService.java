/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.LanguageLevel;
import org.tbbtalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.UpdateLanguageLevelRequest;

public interface LanguageLevelService {

    List<LanguageLevel> listLanguageLevels();

    Page<LanguageLevel> searchLanguageLevels(SearchLanguageLevelRequest request);

    LanguageLevel getLanguageLevel(long id);

    LanguageLevel createLanguageLevel(CreateLanguageLevelRequest request) throws EntityExistsException;

    LanguageLevel updateLanguageLevel(long id, UpdateLanguageLevelRequest request) throws EntityExistsException ;

    boolean deleteLanguageLevel(long id) throws EntityReferencedException;

}
