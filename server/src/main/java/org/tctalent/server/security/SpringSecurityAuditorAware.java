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
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.User;

/**
 * Resolves the current auditor {@link User} for Spring Data JPA auditing.
 * <p>
 * {@link org.tctalent.server.configuration.JpaAuditingConfig} references this bean as
 * {@code auditorProvider}. When {@link org.springframework.data.jpa.domain.support.AuditingEntityListener}
 * handles entity lifecycle callbacks, it calls this {@link AuditorAware} to determine values for
 * {@code @CreatedBy} and {@code @LastModifiedBy}.
 * <p>
 * Strategy:
 * <ul>
 *   <li>Use the logged-in user from {@link AuthService} when available.</li>
 *   <li>Otherwise use a startup-cached system admin id and return a lightweight user shell
 *   (id only), so auditing can write FK values without loading the full User graph.</li>
 * </ul>
 *
 * @author sadatmalik
 */
@Component("auditorProvider")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<User> {

    private final AuthService authService;

    /**
     * Cached once during startup and then read by request threads.
     * Volatile guarantees once startup sets this value, all requests see the latest value,
     * even if the practical risk is near zero.
     */
    private volatile Long systemAdminId;

    /**
     * Called during startup to seed the fallback auditor user id.
     * This avoids querying/loading the full system-admin entity inside auditing callbacks,
     * which can trigger unwanted eager associations during flush.
     */
    public void setSystemAdminId(Long systemAdminId) {
        this.systemAdminId = systemAdminId;
    }

    @NotNull
    @Override
    public Optional<User> getCurrentAuditor() {
        Optional<User> loggedInUser = authService.getLoggedInUser()
            .filter(user -> user.getId() != null);
        if (loggedInUser.isPresent()) {
            return loggedInUser;
        }

        if (systemAdminId != null) {
            // Fallback for anonymous/system-triggered writes: return a lightweight User carrying
            // only the id so Hibernate can write created_by/updated_by without loading User graph.
            User systemAdminUserShell = new User();
            systemAdminUserShell.setId(systemAdminId);
            return Optional.of(systemAdminUserShell);
        }

        // Some tests persist entities before startup auto-create has seeded system admin data.
        return Optional.empty();
    }
}
