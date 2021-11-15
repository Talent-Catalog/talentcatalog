/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.SystemLanguage;
import org.tbbtalent.server.request.language.CreateLanguageRequest;
import org.tbbtalent.server.request.language.SearchLanguageRequest;
import org.tbbtalent.server.request.language.UpdateLanguageRequest;

public interface LanguageService {

    /**
     * Adds a new system language - ie a candidate portal language a user can select
     * @param langCode Code of language to add
     * @return Added SystemLanguage
     * @throws EntityExistsException If the language already exists as a system language
     * @throws NoSuchObjectException If the language code is not known
     */
    SystemLanguage addSystemLanguage(String langCode)
        throws EntityExistsException, NoSuchObjectException;

    List<Language> listLanguages();

    Page<Language> searchLanguages(SearchLanguageRequest request);

    Language getLanguage(long id);

    Language createLanguage(CreateLanguageRequest request) throws EntityExistsException;

    Language updateLanguage(long id, UpdateLanguageRequest request) throws EntityExistsException ;

    boolean deleteLanguage(long id) throws EntityReferencedException;

    List<SystemLanguage> listSystemLanguages();

    Language getLanguage(String languageName);
}
