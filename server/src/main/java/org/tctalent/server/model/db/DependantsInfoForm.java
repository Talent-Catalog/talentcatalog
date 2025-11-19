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

/*
 * Copyright ...
 */
package org.tctalent.server.model.db;

import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;

/**
 * Common form used for loading dependants info.
 */
public class DependantsInfoForm extends CandidateFormInstanceHelper {

    private static final String DEPENDANTS_INFO = "DEPENDANTS_INFO";
    private static final String NO_ELIGIBLE = "NO_ELIGIBLE_FAMILY_MEMBERS";
    private static final String NO_ELIGIBLE_NOTES = "NO_ELIGIBLE_NOTES";

    public DependantsInfoForm(String formName, AuthService authService,
        CandidateService candidateService,
        CandidatePropertyService propertyService) {
        super(formName, authService, candidateService, propertyService);
    }

    public String getFormName() {
        return "DependantsInfoForm";
    }

    public String getFamilyMembersJson() {
        return getProperty(DEPENDANTS_INFO);
    }

    public void setFamilyMembersJson(String json) {
        setProperty(DEPENDANTS_INFO, json);
    }

    public Boolean getNoEligibleFamilyMembers() {
        String v = getProperty(NO_ELIGIBLE);
        return v == null ? null : Boolean.valueOf(v);
    }

    public void setNoEligibleFamilyMembers(Boolean value) {
        setProperty(NO_ELIGIBLE, value == null ? null : value.toString());
    }

    public String getNoEligibleNotes() {
        return getProperty(NO_ELIGIBLE_NOTES);
    }

    public void setNoEligibleNotes(String notes) {
        setProperty(NO_ELIGIBLE_NOTES, notes);
    }
}
