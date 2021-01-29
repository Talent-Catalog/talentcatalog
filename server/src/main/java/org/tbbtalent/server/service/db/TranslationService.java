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

    void updateTranslationFile(String language, Map translations);

    Map<String, Object> getTranslationFile(String language);
}
