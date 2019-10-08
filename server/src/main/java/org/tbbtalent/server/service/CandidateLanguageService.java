package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;

import java.util.List;

public interface CandidateLanguageService {

    CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request);

    void deleteCandidateLanguage(Long id);

    List<CandidateLanguage> list(long id);
}
