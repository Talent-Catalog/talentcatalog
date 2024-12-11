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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;

@Service
public class AuthService {
    private final Set<Role> adminRoles = new HashSet<>(Arrays.asList(
        Role.partneradmin, Role.admin, Role.systemadmin));

    /**
     * Return logged in user. Optional empty if not logged in.
     * <p/>
     * Note that this User object is not fetched from the database - so if you need a live
     * entity associated with a JPA session (so you can fetch linked properties), you should use
     * UserService.getLoggedInUser.
     *
     * @return Logged in user or empty if not logged in.
     */
    public Optional<User> getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof TcUserDetails) {
            return Optional.of(((TcUserDetails) auth.getPrincipal()).getUser());
        }
        return Optional.empty();
    }

    /**
     * Returns id of logged in candidate or null if not logged in
     * @return id of logged in candidate
     */
    public @Nullable Long getLoggedInCandidateId() {
        Long ret = null;
        User user = getLoggedInUser().orElse(null);
        if (user != null) {
            Candidate candidate = user.getCandidate();
            if (candidate != null) {
                ret = candidate.getId();
            }
        }
        return ret;
    }

    /**
     * Returns candidate retrieved from the user context.
     * Note that this Candidate object is not retrieved from the database and
     * therefore will not be part of the JPA persistence context
     * so lazily loaded attributes will not be fetched from the database and
     * will be absent.
     * <p/>
     * It should therefore should only be used as a base for adding new
     * attributes and saving to the database.
     * @return Candidate with no associated attributes
     */
    public @Nullable Candidate getLoggedInCandidate(){
        User user = getLoggedInUser().orElse(null);
        return user == null ? null : user.getCandidate();
    }

    public @Nullable String getUserLanguage() {
        User user = getLoggedInUser().orElse(null);
        return user == null ? null : user.getSelectedLanguage();
    }

    /**
     * Authorizes the logged in user to operate on something that the given user owns.
     * <p/>
     * Currently it is only used to seek authorization for updating a candidate
     * attachment or delete a candidate education record.
     * <ul>
     *     <li>
     *          Readonly users are never authorized.
     *     </li>
     *     <li>
     *          Admins or systemadmins are always authorized
     *     </li>
     *     <li>
     *          partneradmins which have no country restrictions are always authorized. If they
     *          have country restrictions the candidate must be located in one of those countries.
     *     </li>
     *     <li>
     *          If the candidate is also the logged in user, then they are authorized
     *     </li>
     * </ul>
     *
     * @param owner Candidate that owns something that the logged in user is seeking to do something
     *              to.
     * @return True if authorization is granted
     */
    public boolean authoriseLoggedInUser(Candidate owner) {
        User user = getLoggedInUser().orElse(null);
        if (user != null) {
            if (user.getReadOnly()) {
                return false;
            } else if (user.getRole().equals(Role.admin) || user.getRole().equals(Role.systemadmin)) {
                return true;
            } else if (user.getRole().equals(Role.partneradmin)) {
                if (!user.getSourceCountries().isEmpty()) {
                    return user.getSourceCountries().contains(owner.getCountry());
                } else {
                    return true;
                }
            } else if (user.getRole().equals(Role.user)){
                return owner.getId().equals(user.getCandidate().getId());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns true if role is one of the admin roles
     * @param role Role
     * @return True if an admin role
     */
    public boolean hasAdminPrivileges(Role role) {
        return adminRoles.contains(role);
    }
}
