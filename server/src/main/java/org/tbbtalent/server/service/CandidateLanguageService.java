package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.request.language.CreateCandidateLanguageRequest;

public interface CandidateLanguageService {

    CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request);

    void deleteCandidateLanguage(Long id);
}
