package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.repository.CandidateLanguageRepository;
import org.tbbtalent.server.repository.LanguageRepository;
import org.tbbtalent.server.request.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateLanguageService;

@Service
public class CandidateLanguageServiceImpl implements CandidateLanguageService {

    private final CandidateLanguageRepository candidateLanguageRepository;
    private final LanguageRepository languageRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateLanguageServiceImpl(CandidateLanguageRepository candidateLanguageRepository,
                                        LanguageRepository languageRepository,
                                        UserContext userContext) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageRepository = languageRepository;
        this.userContext = userContext;
    }

    @Override
    public CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Load the industry from the database - throw an exception if not found
        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new NoSuchObjectException(Language.class, request.getLanguageId()));

        // Create a new profession object to insert into the database
        CandidateLanguage candidateLanguage = new CandidateLanguage();
        candidateLanguage.setCandidate(candidate);
        candidateLanguage.setLanguage(language);
        candidateLanguage.setSpeak(request.getSpeak());
        candidateLanguage.setReadWrite(request.getReadWrite());

        // Save the profession
        return candidateLanguageRepository.save(candidateLanguage);
    }

    @Override
    public void deleteCandidateLanguage(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateLanguage candidateLanguage = candidateLanguageRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateLanguage.class, id));

        // Check that the user is deleting their own profession
        if (!candidate.getId().equals(candidateLanguage.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        candidateLanguageRepository.delete(candidateLanguage);
    }

}
