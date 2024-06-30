/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Implementation of Spring's {@link UserDetailsService}, returning {@link TcUserDetails}
 * objects.
 */
@Slf4j
@Component
public class TcUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public TcUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public TcUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user found for: " + username);
        }

        LogBuilder.builder(log)
            .action("loadUserByUsername")
            .message("Found user with ID " + user.getId() + " for username '" + username + "'")
            .logDebug();

        return new TcUserDetails(user);
    }

}
