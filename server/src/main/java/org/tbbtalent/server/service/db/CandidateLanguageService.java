/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;

public interface CandidateLanguageService {

    CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request);

    CandidateLanguage updateCandidateLanguage(Long id, UpdateCandidateLanguageRequest request);

    void deleteCandidateLanguage(Long id);

    List<CandidateLanguage> list(long id);

    List<CandidateLanguage> updateCandidateLanguages(UpdateCandidateLanguagesRequest request);
}
