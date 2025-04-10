/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.data.domain.Page;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tctalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tctalent.server.request.language.level.UpdateLanguageLevelRequest;

public interface LanguageLevelService {

    List<LanguageLevel> listLanguageLevels();

    Page<LanguageLevel> searchLanguageLevels(SearchLanguageLevelRequest request);

    LanguageLevel getLanguageLevel(long id);

    LanguageLevel createLanguageLevel(
        CreateLanguageLevelRequest request) throws EntityExistsException;

    LanguageLevel updateLanguageLevel(long id, UpdateLanguageLevelRequest request) throws EntityExistsException ;

    boolean deleteLanguageLevel(long id) throws EntityReferencedException;

    LanguageLevel findByLevel(int level);
}
