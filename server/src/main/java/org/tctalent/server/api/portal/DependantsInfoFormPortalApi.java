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

package org.tctalent.server.api.portal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.DependantsInfoForm;
import org.tctalent.server.request.form.DependantsInfoFormData;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * API endpoint for Dependants Info Form under the portal.
 */
@RestController
@RequestMapping("/api/portal/form/dependants-info-form")
@RequiredArgsConstructor
public class DependantsInfoFormPortalApi {

    private final AuthService authService;
    private final CandidateService candidateService;
    private final CandidatePropertyService candidatePropertyService;

    @PostMapping
    @NotNull
    public Map<String, Object> createOrUpdate(@Valid @RequestBody DependantsInfoFormData request) {
        DependantsInfoForm form = new DependantsInfoForm(
            "DependantsInfoForm", authService, candidateService, candidatePropertyService);

        form.setFamilyMembersJson(request.getFamilyMembersJson());
        form.setNoEligibleFamilyMembers(request.getNoEligibleFamilyMembers());
        form.setNoEligibleNotes(request.getNoEligibleNotes());

        return dependantsInfoFormDto().build(form);
    }

    @GetMapping
    @NotNull
    public Map<String, Object> get() {
        DependantsInfoForm form = new DependantsInfoForm(
            "DependantsInfoForm", authService, candidateService, candidatePropertyService);
        return dependantsInfoFormDto().build(form);
    }

    private DtoBuilder dependantsInfoFormDto() {
        return new DtoBuilder()
            .add("familyMembersJson")
            .add("noEligibleFamilyMembers")
            .add("noEligibleNotes");
    }
}
