// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.repository.db;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.model.db.CandidateFormInstance;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateFormService;
import org.tctalent.server.service.db.CandidateService;

/**
 * <p>
 *   This handles events for any Repositories for subclasses of {@link CandidateFormInstance}
 * </p>
 * <p>
 *     The main purpose is to populate the Candidate field of an instance with the currently
 *     logged-in candidate. This would normally be done in the controller API, but that
 *     is not possible when using Spring Data Rest because Spring Data Rest automatically creates
 *     the API controller for CRUD operations when a repository is annotated
 *     as a RepositoryRestResource.
 * </p>
 * <p>
 *   To populate the candidate field we add an event handler which processes the instance before
 *   it is saved by the repository - looking up the currently logged in candidate and adding
 *   it to the instance.
 * </p>
 * <p>
 *   It also provides CandidateFormInstance createOrUpdate semantics (not normally provide by
 *   a repository) by automatically creating an instance if needed before each update.
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
    private final CandidateFormInstanceRepository candidateFormInstanceRepository;

    @HandleBeforeCreate
    public void beforeCreate(CandidateFormInstance candidateFormInstance) {
        if (candidateFormInstance.getCandidate() == null) {
            Candidate candidate = getLoggedInCandidate();

            //TODO JC Copy any pendingCandidate contents across to the candidate

            String formName = candidateFormInstance.getFormName();
            CandidateForm candidateForm = candidateFormService.getByName(formName);
            candidateFormInstance.setCandidateForm(candidateForm);

            candidateFormInstance.setCandidate(candidate);
        }
    }

    /**
     * <p>
     * Hook to automatically populate and instance's candidate property with the currently logged
     * in candidate.
     * </p>
     * <p>
     * It also auto creates a blank instance if one doesn't exist.
     * This gives us createOrUpdate semantics.
     * </p>
     * @param candidateFormInstance Instance that is about to be saved
     */
    @HandleBeforeSave
    public void beforeSavePreCreateInstanceIfNeeded(CandidateFormInstance candidateFormInstance) {
        //Add instance with the currently logged in candidate if the instance doesn't have a candidate
        if (candidateFormInstance.getCandidate() == null) {
            Candidate candidate = getLoggedInCandidate();

            //TODO JC Copy any pendingCandidate contents across to the candidate

            candidateFormInstance.setCandidate(candidate);
        }

        //Create an instance on the database if one doesn't already exist.
        final CandidateForm candidateForm = candidateFormInstance.getCandidateForm();
        final Candidate candidate = candidateFormInstance.getCandidate();

        //Compute the key
        CandidateFormInstanceKey key =
            new CandidateFormInstanceKey(candidate.getId(), candidateForm.getId());

        //Check whether an instance already exists.
        final Optional<CandidateFormInstance> optInstance =
            candidateFormInstanceRepository.findById(key);

        //If not create one
        if (optInstance.isEmpty()) {
            //No instance exists, add one.
            candidateFormInstanceRepository.save(candidateFormInstance);
        }
    }

    private Candidate getLoggedInCandidate() {
        Long loggedInCandidateId = authService.getLoggedInCandidateId();
        if (loggedInCandidateId == null) {
            throw new InvalidSessionException("Not logged in");
        }
        return candidateService.getCandidate(loggedInCandidateId);
    }

}
