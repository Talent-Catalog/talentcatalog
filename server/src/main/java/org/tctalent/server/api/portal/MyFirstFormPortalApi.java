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

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.admin.ITableApi;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.MyFirstForm;
import org.tctalent.server.request.form.MyFirstFormUpdateRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateFormInstanceService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@RestController
@RequestMapping("/api/portal/form/my-first-form")
@RequiredArgsConstructor
public class MyFirstFormPortalApi
    implements ITableApi<MyFirstFormUpdateRequest, MyFirstFormUpdateRequest, MyFirstFormUpdateRequest> {

    private final AuthService authService;
    private final CandidateFormInstanceService formService;

    @Override
    public @NotNull Map<String, Object> create(MyFirstFormUpdateRequest request)
        throws EntityExistsException {
        Long loggedInCandidateId = authService.getLoggedInCandidateId();
        if (loggedInCandidateId == null) {
            throw new InvalidSessionException("Not logged in");
        }

        MyFirstForm form = this.formService.createMyFirstForm(loggedInCandidateId, request);
        return myFirstFormDto().build(form);
    }

    private DtoBuilder myFirstFormDto() {
        return new DtoBuilder()
            .add("candidateNumber")
            .add("hairColour");
    }

}
