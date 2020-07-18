package org.tbbtalent.server.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.LanguageLevel;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.repository.db.CandidateLanguageRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.LanguageLevelRepository;
import org.tbbtalent.server.repository.db.LanguageRepository;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateLanguageService;

@Service
public class CandidateLanguageServiceImpl implements CandidateLanguageService {

    private final CandidateLanguageRepository candidateLanguageRepository;
    private final CandidateRepository candidateRepository;
    private final LanguageRepository languageRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateLanguageServiceImpl(CandidateLanguageRepository candidateLanguageRepository,
                                        LanguageRepository languageRepository,
                                        CandidateRepository candidateRepository,
                                        LanguageLevelRepository languageLevelRepository,
                                        UserContext userContext) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageRepository = languageRepository;
        this.candidateRepository = candidateRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.userContext = userContext;
    }

    @Override
    public CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Load the industry from the database - throw an exception if not found
        Language language = languageRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(Language.class, request.getId()));

        // Load the languageLevels from the database - throw an exception if not found
        LanguageLevel languageSpeak = languageLevelRepository.findById(request.getSpokenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getSpokenLevelId()));

        LanguageLevel languageReadWrite = languageLevelRepository.findById(request.getWrittenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getWrittenLevelId()));

        // Create a new candidateOccupation object to insert into the database
        CandidateLanguage candidateLanguage = new CandidateLanguage();
        candidateLanguage.setCandidate(candidate);
        candidateLanguage.setLanguage(language);
        candidateLanguage.setSpokenLevel(languageSpeak);
        candidateLanguage.setWrittenLevel(languageReadWrite);

        // Save the candidateOccupation
        candidateLanguage = candidateLanguageRepository.save(candidateLanguage);
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
        return candidateLanguage;
    }

    @Override
    public CandidateLanguage updateCandidateLanguage(Long id, UpdateCandidateLanguageRequest request) {

        CandidateLanguage candidateLanguage = candidateLanguageRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateLanguage.class, id));

        // Load the language from the database - throw an exception if not found
        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new NoSuchObjectException(Language.class, request.getLanguageId()));

        // Load the levels from the database - throw an exception if not found
        LanguageLevel spokenLevel = languageLevelRepository.findById(request.getSpokenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getSpokenLevelId()));

        LanguageLevel writtenLevel = languageLevelRepository.findById(request.getWrittenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getWrittenLevelId()));

        // Update education object to insert into the database
        candidateLanguage.setLanguage(language);
        candidateLanguage.setSpokenLevel(spokenLevel);
        candidateLanguage.setWrittenLevel(writtenLevel);

        // Save the candidateOccupation
        return candidateLanguageRepository.save(candidateLanguage);
    }

    @Override
    public void deleteCandidateLanguage(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateLanguage candidateLanguage = candidateLanguageRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateLanguage.class, id));

        // Check that the user is deleting their own candidateOccupation
        if (!candidate.getId().equals(candidateLanguage.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }
        candidateLanguageRepository.delete(candidateLanguage);
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
    }

    @Override
    public List<CandidateLanguage> list(long id) {
        return candidateLanguageRepository.findByCandidateId(id);
    }

    @Override
    public List<CandidateLanguage> updateCandidateLanguages(UpdateCandidateLanguagesRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        List<CandidateLanguage> updatedLanguages = new ArrayList<>();
        List<Long> updatedLanguageIds = new ArrayList<>();

        List<CandidateLanguage> candidateLanguages = candidateLanguageRepository.findByCandidateId(candidate.getId());
        Map<Long, CandidateLanguage> map = candidateLanguages.stream().collect( Collectors.toMap(CandidateLanguage::getId,
                Function.identity()) );

        Map<Long, LanguageLevel> languageLevels = languageLevelRepository.findByStatus(Status.active).stream()
                .collect(Collectors.toMap(LanguageLevel::getId, Function.identity()));

        for (UpdateCandidateLanguageRequest update : request.getUpdates()) {
            /* Check if language has been previously saved */
            CandidateLanguage candidateLanguage = update.getLanguageId() != null ? map.get(update.getLanguageId()) : null;
            if (candidateLanguage != null){
                /* Check if the language has changed */
                if (!update.getLanguageId().equals(candidateLanguage.getLanguage().getId())){
                    Language language = languageRepository.findById(update.getLanguageId())
                            .orElseThrow(() -> new NoSuchObjectException(Language.class, update.getLanguageId()));
                    candidateLanguage.setLanguage(language);
                }
                candidateLanguage.setSpokenLevel(languageLevels.get(update.getSpokenLevelId()));
                candidateLanguage.setWrittenLevel(languageLevels.get(update.getWrittenLevelId()));
            } else {
                /* Create a new candidate language */
                Language language = languageRepository.findById(update.getLanguageId())
                        .orElseThrow(() -> new NoSuchObjectException(Language.class, update.getLanguageId()));
                candidateLanguage = new CandidateLanguage(
                        candidate,
                        language,
                        languageLevels.get(update.getWrittenLevelId()),
                        languageLevels.get(update.getSpokenLevelId())
                );
            }
            updatedLanguages.add(candidateLanguageRepository.save(candidateLanguage));
            updatedLanguageIds.add(candidateLanguage.getId());
        }

        for (Long existingCandidateLanguageId : map.keySet()) {
            /* Remove existing database entries that aren't present in the request */
            if (!updatedLanguageIds.contains(existingCandidateLanguageId)){
                candidateLanguageRepository.deleteById(existingCandidateLanguageId);
            }
        }

        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);

        return candidateLanguages;
    }
}
