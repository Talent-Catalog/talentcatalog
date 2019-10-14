package org.tbbtalent.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.UserRepository;

@Service
public class UserContext {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    public UserContext(UserRepository userRepository, CandidateRepository candidateRepository) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
    }

    public User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) auth.getPrincipal()).getUser();
        }
        return null;
    }

    public Candidate getLoggedInCandidate(){
        User user = getLoggedInUser();
        return user.getCandidate();
    }
    
    public String getUserLanguage() {
        User user = getLoggedInUser();
        return user.getSelectedLanguage();
    }
}
