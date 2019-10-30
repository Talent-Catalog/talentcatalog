package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;

import java.util.List;

public interface CandidateLanguageService {

    CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request);

    CandidateLanguage updateCandidateLanguage(Long id, UpdateCandidateLanguageRequest request);

    void deleteCandidateLanguage(Long id);

    List<CandidateLanguage> list(long id);

    List<CandidateLanguage> updateCandidateLanguages(UpdateCandidateLanguagesRequest request);
}
