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

import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;

import java.util.List;

public interface CandidateLanguageService {

    CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request);

    CandidateLanguage updateCandidateLanguage(UpdateCandidateLanguageRequest request);

    List<CandidateLanguage> updateCandidateLanguages(UpdateCandidateLanguagesRequest request);

    void deleteCandidateLanguage(Long id);

    List<CandidateLanguage> list(long id);
}
