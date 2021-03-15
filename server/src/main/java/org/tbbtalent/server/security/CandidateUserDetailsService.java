/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.UserRepository;

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
