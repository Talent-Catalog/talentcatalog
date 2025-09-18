// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

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
