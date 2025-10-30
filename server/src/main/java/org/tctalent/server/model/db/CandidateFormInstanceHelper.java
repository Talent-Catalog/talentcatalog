/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.model.db;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;

/**
 * Helper class for candidate form instances.
 * <p/>
 * Candidate forms should extend this class.
 *
 * @author John Cameron
 */
@Setter
@Getter
public class CandidateFormInstanceHelper {

    private final String formName;
    private final AuthService authService;
    private final CandidateService candidateService;
    private final CandidatePropertyService propertyService;

    private Candidate candidate;

    CandidateFormInstanceHelper(String formName, AuthService authService, CandidateService candidateService,
        CandidatePropertyService propertyService) {
        this.formName = formName;
        this.authService = authService;
        this.candidateService = candidateService;
        this.propertyService = propertyService;
    }

    public Candidate getCandidate() {
        if (candidate == null) {
            Long loggedInCandidateId = authService.getLoggedInCandidateId();
            if (loggedInCandidateId == null) {
                throw new IllegalStateException("Not logged in");
            }
            candidate = candidateService.getCandidate(loggedInCandidateId);
        }
        return candidate;
    }

    /**
     * Return value of given property name from candidate properties.
     * @param propertyName Name of property
     * @return Value of property - null if not found
     */
    protected String getProperty(String propertyName) {
        String value = null;
        Map<String,CandidateProperty> properties = getCandidate().getCandidateProperties();
        if (properties != null) {
            CandidateProperty property = properties.get(propertyName);
            if (property != null) {
                value = property.getValue();
            }
        }
        return value;
    }

    /**
     * Set value of given property name from candidate properties.
     * @param propertyName Name of property
     * @param value New property value - may be null. Replaces any previous value.
     */
    protected void setProperty(String propertyName, String value) {
        propertyService.createOrUpdateProperty(getCandidate(), propertyName, value, null);
    }

    public void save() {
        candidateService.save(getCandidate(), false, false);
    }
}
