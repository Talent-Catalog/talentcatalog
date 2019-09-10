package org.tbbtalent.server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.repository.CandidateRepository;

@Component
public class CandidateUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CandidateUserDetailsService.class);

    private final CandidateRepository candidateRepository;

    @Autowired
    public CandidateUserDetailsService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    public AuthenticatedCandidate loadUserByUsername(String username) throws UsernameNotFoundException {

        /* Handle JWT token parsing */
        Candidate candidate = candidateRepository.findByCandidateNumber(username);
        /* Handle authentication */
        if (candidate == null) {
            candidate = candidateRepository.findByAnyUserIdentityIgnoreCase(username);
        }
        if (candidate == null) {
            throw new UsernameNotFoundException("No candidate found for: " + username);
        }

        log.debug("Found candidate with ID {} for username '{}'", candidate.getId(), username);
        return new AuthenticatedCandidate(candidate);
    }

}
