// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.repository.db;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.model.db.CandidateFormInstance;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateFormService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;

/**
 * <p>
 *   This handles events for any Repositories for subclasses of {@link CandidateFormInstance}
 * </p>
 * <p>
 *     It is necessary only to handle creating brand new CandidateFormInstance's.
 *     The standard Spring Data Rest processing handles updates fine.
 * </p>
 * <p>
 *     For creating new instances, the main purpose is to populate the Candidate field of an
 *     instance with the currently logged-in candidate and the CandidateForm field.
 *     This would normally be done in the controller API, but that is not possible when using
 *     Spring Data Rest because Spring Data Rest automatically creates the API controller for CRUD
 *     operations when a repository is annotated as a RepositoryRestResource.
 * </p>
 * <p>
 *   To populate these fields, we add an event handler which processes the instance before
 *   it is created by the repository.
 * </p>
 * <p>
 *   The handler also copies initial field values provided in the create API call.
 *   These are stored in a pendingCandidate object.
 * </p>
 * @author John Cameron
 */
@RequiredArgsConstructor
@Component
@RepositoryEventHandler()
public class CandidateFormInstanceRepoEventHandler {
    private final AuthService authService;
    private final CandidateService candidateService;
    private final CandidateFormService candidateFormService;
    private final CandidatePropertyService candidatePropertyService;

    @HandleBeforeCreate
    public void beforeCreate(CandidateFormInstance candidateFormInstance) {

        if (candidateFormInstance.getCandidateForm() == null) {
            String formName = candidateFormInstance.getFormName();
            CandidateForm candidateForm = candidateFormService.getByName(formName);
            candidateFormInstance.setCandidateForm(candidateForm);
        }

        if (candidateFormInstance.getCandidate() == null) {
            Candidate candidate = getLoggedInCandidate();

            //Copy any pendingCandidate contents across to the candidate
            Candidate pendingCandidate = candidateFormInstance.getPendingCandidate();
            if (pendingCandidate != null) {
                //TODO JC What about other stuff - need Mapper
                candidate.setCity( pendingCandidate.getCity() );

                //Copy across properties
                //Note that we can't just simply transfer properties directly from one candidate
                //object to another because each property is associated with a particular candidate.
                //We have to use the CandidatePropertyService.
                Map<String, CandidateProperty> pendingProperties = pendingCandidate.getCandidateProperties();
                if (pendingProperties != null) {
                    for (Entry<String, CandidateProperty> entry : pendingProperties.entrySet()) {
                        candidatePropertyService.createOrUpdateProperty(
                            candidate, entry.getKey(), entry.getValue().getValue(), null);
                    }
                }
            }

            candidateFormInstance.setCandidate(candidate);
        }

        candidateFormInstance.setCreatedDate(OffsetDateTime.now());
    }

    private Candidate getLoggedInCandidate() {
        Long loggedInCandidateId = authService.getLoggedInCandidateId();
        if (loggedInCandidateId == null) {
            throw new InvalidSessionException("Not logged in");
        }
        return candidateService.getCandidate(loggedInCandidateId);
    }

}
