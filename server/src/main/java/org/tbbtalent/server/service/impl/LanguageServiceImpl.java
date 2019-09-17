package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.repository.LanguageRepository;
import org.tbbtalent.server.request.language.CreateLanguageRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.LanguageService;

@Service
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final UserContext userContext;

    @Autowired
    public LanguageServiceImpl(LanguageRepository languageRepository,
                                 UserContext userContext) {
        this.languageRepository = languageRepository;
        this.userContext = userContext;
    }


    @Override
    public Language createLanguage(CreateLanguageRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Create a new profession object to insert into the database
        Language language = new Language();
        language.setCandidate(candidate);
        language.setName(request.getName());
        language.setSpeak(request.getSpeak());
        language.setReadWrite(request.getReadWrite());

        // Save the profession
        return languageRepository.save(language);
    }

}
