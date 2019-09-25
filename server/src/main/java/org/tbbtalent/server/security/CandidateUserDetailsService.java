package org.tbbtalent.server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.UserRepository;

@Component
public class CandidateUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CandidateUserDetailsService.class);

    private final UserRepository userRepository;

    @Autowired
    public CandidateUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {

        /* Handle JWT token parsing */
        User user = userRepository.findByUsernameIgnoreCase(username);
        /* Handle authentication */
        if (user == null) {
            throw new UsernameNotFoundException("No user found for: " + username);
        }

        log.debug("Found candidate with ID {} for username '{}'", user.getId(), username);
        return new AuthenticatedUser(user);
    }

}
