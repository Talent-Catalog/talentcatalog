package org.tbbtalent.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.repository.CandidateRepository;

@Service
public class UserContext {

    private final CandidateRepository candidateRepository;

    @Autowired
    public UserContext(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public Candidate getLoggedInCandidate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedCandidate) {
            // we have to look the user up in order to attach it to the current Hibernate session
            long userId = ((AuthenticatedCandidate) auth.getPrincipal()).getCandidate().getId();
            return this.candidateRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Missing user with ID: " + userId));
        }
        return null;
    }
}
