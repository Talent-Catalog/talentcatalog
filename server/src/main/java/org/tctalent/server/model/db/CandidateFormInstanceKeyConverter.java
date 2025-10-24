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

import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.service.db.CandidateFormService;
import org.tctalent.server.service.db.CandidateService;

/**
 * Converts the string key sent to the API into a composite key needed to access CandidateFormInstance's.
 *
 * @author John Cameron
 */
@RequiredArgsConstructor
@Component
public class CandidateFormInstanceKeyConverter implements BackendIdConverter {
    private final CandidateService candidateService;
    private final CandidateFormService candidateFormService;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return CandidateFormInstance.class.isAssignableFrom(type);
    }

    @Override
    public Serializable fromRequestId(String formName, Class<?> entityType) {
        Candidate candidate = candidateService.getLoggedInCandidate()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        CandidateForm candidateForm = candidateFormService.getByName(formName);
        return new CandidateFormInstanceKey(candidate.getId(), candidateForm.getId());
    }

    @Override
    public String toRequestId(Serializable id, Class<?> entityType) {
        CandidateFormInstanceKey pid = (CandidateFormInstanceKey) id;
        CandidateForm candidateForm = candidateFormService.get(pid.getFormId());
        return candidateForm.getName();
    }
}
