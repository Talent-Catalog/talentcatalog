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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private CandidateRepository candidateRepository;

    @Autowired
    public void setCandidateRepository(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)) {
            return false;
        }
        Candidate owner = candidateRepository.findByIdLoadUser((Long) targetDomainObject).orElse(null);
        return hasPrivilege(auth, owner);
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }

        Candidate owner = candidateRepository.findByIdLoadUser((Long) targetId, null).orElse(null);
        return hasPrivilege(auth, owner);
    }

    private boolean hasPrivilege(Authentication auth, @Nullable Candidate owner) {
        if (owner == null) {
            return false;
        }
        User loggedInUser = ((TbbUserDetails) auth.getPrincipal()).getUser();
        if (loggedInUser != null) {
            if (loggedInUser.getReadOnly()) {
                return false;
            } else if (loggedInUser.getRole().equals(Role.admin)) {
                return true;
            } else if (loggedInUser.getRole().equals(Role.sourcepartneradmin)) {
                if (!loggedInUser.getSourceCountries().isEmpty()) {
                    return loggedInUser.getSourceCountries().contains(owner.getCountry());
                } else {
                    return true;
                }
            } else if (loggedInUser.getRole().equals(Role.user)){
                return owner.getId().equals(loggedInUser.getCandidate().getId());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
