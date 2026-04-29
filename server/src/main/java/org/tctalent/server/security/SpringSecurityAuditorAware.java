/*
 * Copyright (c) 2024 Talent Catalog.
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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.service.db.UserService;

/**
 * Implementation of AuditorAware that retrieves the current user from the AuthService. If no user
 * is logged in, it falls back to a system admin user. This class is used for JPA auditing to
 * automatically populate createdBy and lastModifiedBy fields in entities.
 *
 * @author sadatmalik
 */
@Component("auditorProvider")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<User> {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ObjectProvider<UserService> userServiceProvider;

    @NotNull
    @Override
    public Optional<User> getCurrentAuditor() {
        Optional<User> loggedInUser = authService.getLoggedInUser();
        if (loggedInUser.isPresent()) {
            return resolveManagedUser(loggedInUser.get());
        }

        try {
            User systemAdminUser = userServiceProvider.getObject().getSystemAdminUser();
            return resolveManagedUser(systemAdminUser);
        } catch (RuntimeException ex) {
            // Some tests persist entities without seeded system admin users.
            return Optional.empty();
        }
    }

    private Optional<User> resolveManagedUser(User user) {
        if (user == null || user.getId() == null) {
            return Optional.empty();
        }
        return userRepository.findById(user.getId());
    }
}
