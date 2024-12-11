/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateSavedListKey;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.request.candidate.IHasSetOfSavedLists;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateCandidateShareableDocsRequest;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.list.ContentUpdateType;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;

@Service
@Slf4j
public class CandidateSavedListServiceImpl implements CandidateSavedListService {
    private final AuthService authService;
    private final CandidateService candidateService;
    private final FileSystemService fileSystemService;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SavedListService savedListService;
    private final SavedListRepository savedListRepository;
    private final UserService userService;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final CandidateSavedListRepository candidateSavedListRepository;

    public CandidateSavedListServiceImpl(
        AuthService authService, CandidateService candidateService,
        FileSystemService fileSystemService,
        SalesforceJobOppService salesforceJobOppService, SavedListService savedListService,
        SavedListRepository savedListRepository,
        UserService userService,
        CandidateAttachmentRepository candidateAttachmentRepository,
        CandidateSavedListRepository candidateSavedListRepository) {
        this.authService = authService;
        this.candidateService = candidateService;
        this.fileSystemService = fileSystemService;
        this.salesforceJobOppService = salesforceJobOppService;
        this.savedListService = savedListService;
        this.savedListRepository = savedListRepository;
        this.userService = userService;
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.candidateSavedListRepository = candidateSavedListRepository;
    }

    @Override
    public void clearCandidateSavedLists(Candidate candidate) {
        Set<SavedList> savedLists = candidate.getSavedLists();
        for (SavedList savedList : savedLists) {
            savedListService.removeCandidateFromList(candidate, savedList);
        }
    }

    @Override
    public boolean clearCandidateSavedLists(long candidateId) {
        Candidate candidate = candidateService.findByIdLoadSavedLists(candidateId);

        boolean done = true;
        if (candidate == null) {
            done = false;
        } else {
            clearCandidateSavedLists(candidate);
        }
        return done;
    }

    @Override
    public void clearSavedListCandidates(SavedList savedList) {
        Set<Candidate> candidates = savedList.getCandidates();
        for (Candidate candidate : candidates) {
            savedListService.removeCandidateFromList(candidate, savedList);
        }
    }

    @Override
    public boolean clearSavedList(long savedListId) throws InvalidRequestException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
            .orElse(null);

        boolean done = true;
        if (savedList == null) {
            done = false;
        } else {
            clearSavedListCandidates(savedList);
        }
        return done;
    }

    @Override
    public SavedList copy(long id, CopySourceContentsRequest request)
        throws EntityExistsException, NoSuchObjectException {
        SavedList sourceList = savedListRepository.findByIdLoadCandidates(id)
            .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));
        return copy(sourceList, request);
    }

    @Override
    public SavedList copy(SavedList sourceList, CopySourceContentsRequest request)
        throws EntityExistsException, NoSuchObjectException {
        SavedList targetList;
        final Long targetId = request.getSavedListId();
        boolean newList = targetId == 0;
        if (newList) {
            //Request is to create a new list
            //Name for this new list will be in the newListName field - copy that down to the
            //name field which is where the standard createSavedList is expecting to find the name.
            request.setName(request.getNewListName());
            targetList = savedListService.createSavedList(request);
        } else {
            targetList = savedListRepository.findByIdLoadCandidates(targetId)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, targetId));
        }

        boolean replace = request.getUpdateType() == ContentUpdateType.replace;
        //New or replaced list inherits source's savedSearchSource, if any
        if (newList || replace) {
            final SavedSearch savedSearchSource = sourceList.getSavedSearchSource();
            if (savedSearchSource != null) {
                targetList.setSavedSearchSource(savedSearchSource);
            }
        }

        //Copy across list contents (which includes context notes)
        copyContents(sourceList, targetList, replace);

        return targetList;
    }

    @Override
    public void copyContents(
        SavedList source, SavedList destination, boolean replace) {
        //Get candidates in source list
        final Set<Candidate> candidates = source.getCandidates();

        //Add or replace them to destination as requested.
        if (replace) {
            clearSavedList(destination.getId());
        }
        savedListService.addCandidatesToList(destination, candidates, source);

        savedListService.saveIt(destination);
    }

    @Override
    public void copyContents(UpdateExplicitSavedListContentsRequest request,
        SavedList destination) {

        if (request.getUpdateType() == ContentUpdateType.replace) {
            clearSavedList(destination.getId());
        }

        //Retrieve source list, if any
        SavedList sourceList = savedListService.fetchSourceList(request);

        //New list inherits source's savedSearchSource, if any
        if (sourceList != null) {
            final SavedSearch savedSearchSource = sourceList.getSavedSearchSource();
            if (savedSearchSource != null) {
                destination.setSavedSearchSource(savedSearchSource);
            }
        }

        //Retrieve candidates
        Set<Candidate> candidates = savedListService.fetchCandidates(request);

        //Add candidates to created list, together with any context if source
        //list was supplied.
        savedListService.addCandidatesToList(destination, candidates, sourceList);

        savedListService.saveIt(destination);
    }

    @Override
    @Transactional
    public boolean deleteSavedList(long savedListId) {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
            .orElse(null);

        final User loggedInUser = authService.getLoggedInUser().orElse(null);
        if (savedList != null && loggedInUser != null) {

            // Check if user owns this list
            if(savedList.getCreatedBy().getId().equals(loggedInUser.getId())) {

                //Need to clear out many to many relationships before deleting
                //the list otherwise we will have other entities pointing to
                //this list.
                clearSavedList(savedList.getId());
                savedList.setWatcherIds(null);
                savedList.setUsers(null);

                //Delete list
                savedListRepository.delete(savedList);

                return true;
            } else {
                throw new InvalidRequestException("You can't delete other user's saved lists.");
            }

        }
        return false;
    }

    @Override
    public boolean mergeCandidateSavedLists(long candidateId, IHasSetOfSavedLists request) {
        Candidate candidate = candidateService.findByIdLoadSavedLists(candidateId);

        boolean done = true;
        if (candidate == null) {
            done = false;
        } else {
            Set<SavedList> savedLists = fetchSavedLists(request);
            candidate.addSavedLists(savedLists);

            candidateService.saveIt(candidate);
        }
        return done;
    }

    /**
     * Changes the permissions of the given document to viewable by anyone.
     * @param doc Document to be published
     * @throws UnauthorisedActionException if the changing of the file permissions failed.
     */
    private void publishDoc(@Nullable CandidateAttachment doc)
        throws UnauthorisedActionException {
        if (doc != null && doc.getType() == AttachmentType.googlefile) {
            GoogleFileSystemFile file = new GoogleFileSystemFile(doc.getUrl());
            try {
                fileSystemService.publishFile(file);
            } catch (IOException e) {
                throw new UnauthorisedActionException("file permission");
            }
        }
    }

    @Override
    public boolean removeFromCandidateSavedLists(long candidateId, IHasSetOfSavedLists request) {
        Candidate candidate = candidateService.findByIdLoadSavedLists(candidateId);

        boolean done = true;
        if (candidate == null) {
            done = false;
        } else {
            Set<SavedList> savedLists = fetchSavedLists(request);
            for (SavedList savedList : savedLists) {
                savedListService.removeCandidateFromList(candidate, savedList);
            }
        }
        return done;
    }

    private @NotNull Set<SavedList> fetchSavedLists(IHasSetOfSavedLists request)
        throws NoSuchObjectException {

        Set<SavedList> savedLists = new HashSet<>();

        Set<Long> savedListIds = request.getSavedListIds();
        if (savedListIds != null) {
            for (Long savedListId : savedListIds) {
                SavedList savedList = savedListService.get(savedListId);
                savedLists.add(savedList);
            }
        }

        return savedLists;
    }

    @Override
    public void updateCandidateContextNote(
            long savedListId, UpdateCandidateContextNoteRequest request) {
        CandidateSavedListKey key =
                new CandidateSavedListKey(request.getCandidateId(), savedListId);
        CandidateSavedList csl = candidateSavedListRepository.findById(key)
                .orElse(null);
        if (csl != null) {
            csl.setContextNote(request.getContextNote());
            candidateSavedListRepository.save(csl);
        }
    }

    @Override
    public Candidate updateShareableDocs(long id, UpdateCandidateShareableDocsRequest request)
        throws UnauthorisedActionException {
        User loggedInUser = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = userService.getDefaultSourceCountries(loggedInUser);
        Candidate candidate = candidateService.findByIdLoadUser(id, sourceCountries);
        if (candidate == null) {
            throw new NoSuchObjectException(Candidate.class, id);
        }

        CandidateAttachment cv = null;
        if (request.getShareableCvAttachmentId() != null) {
            cv = candidateAttachmentRepository.findById(request.getShareableCvAttachmentId())
                .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getShareableCvAttachmentId()));

            validateDocBelongsToCandidate(candidate, cv);
        }

        CandidateAttachment doc = null;
        if (request.getShareableDocAttachmentId() != null) {
            doc = candidateAttachmentRepository.findById(request.getShareableDocAttachmentId())
                .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getShareableDocAttachmentId()));

            validateDocBelongsToCandidate(candidate, doc);
        }

        //Publish these docs (if not null) - ie make them viewable by anyone.
        publishDoc(cv);
        publishDoc(doc);

        if (request.getSavedListId() != null) {
            //Request is to update the shareable docs associated with a candidate and list
            updateCandidateShareableDocs(id, request.getSavedListId(), cv, doc);

            // If the candidate's shareable docs are null, set them with the list's shareable docs.
            if (candidate.getShareableCv() == null) {
                candidate.setShareableCv(cv);
            }
            if (candidate.getShareableDoc() == null) {
                candidate.setShareableDoc(doc);
            }
        } else {
            //Request is just to update the candidate's shareable doc
            candidate.setShareableCv(cv);
            candidate.setShareableDoc(doc);
        }
        return candidateService.save(candidate, true);
    }

    @Override
    public void updateCandidateShareableDocs(long candidateId, long savedListId, CandidateAttachment cv, CandidateAttachment doc) {
        CandidateSavedListKey key =
                new CandidateSavedListKey(candidateId, savedListId);
        CandidateSavedList csl = candidateSavedListRepository.findById(key)
                .orElse(null);
        if (csl != null) {
            csl.setShareableCv(cv);
            csl.setShareableDoc(doc);
            candidateSavedListRepository.save(csl);
        }
    }

    /**
     * Validation to make sure a candidate's shareable doc belongs to the candidate.
     * @param candidate Candidate that the request is associated to
     * @param doc Candidate Attachment that is attempted to be set as a shareable doc of the candidate.
     */
    private void validateDocBelongsToCandidate(Candidate candidate, CandidateAttachment doc) {
        if (doc.getCandidate() != candidate) {
            throw new InvalidRequestException("The document '" + doc.getName()
                    + "' does not belong to the candidate " + candidate.getCandidateNumber());
        }
    }
}
