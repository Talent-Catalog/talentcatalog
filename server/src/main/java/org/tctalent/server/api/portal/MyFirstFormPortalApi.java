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
import org.tctalent.server.model.db.MyFirstForm;
import org.tctalent.server.request.form.MyFirstFormData;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/portal/form/my-first-form")
@RequiredArgsConstructor
public class MyFirstFormPortalApi {

    private final AuthService authService;
    private final CandidateService candidateService;
    private final CandidatePropertyService candidatePropertyService;

    @PostMapping
    @NotNull
    public Map<String, Object> createOrUpdate(@Valid @RequestBody MyFirstFormData request) {

        MyFirstForm form = new MyFirstForm(
            "MyFirstForm", authService, candidateService, candidatePropertyService);

        form.setCity(request.getCity());
        form.setHairColour(request.getHairColour());
        form.save();

        return myFirstFormDto().build(form);
    }

    @GetMapping
    @NotNull
    public Map<String, Object> get() {
        MyFirstForm form = new MyFirstForm(
            "MyFirstForm", authService, candidateService, candidatePropertyService);
        return myFirstFormDto().build(form);
    }

    private DtoBuilder myFirstFormDto() {
        return new DtoBuilder()
            .add("city")
            .add("hairColour");
    }
}
