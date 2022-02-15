/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.UnauthorisedActionException;
import org.tbbtalent.server.model.db.AttachmentType;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.model.db.CandidateSavedList;
import org.tbbtalent.server.model.db.CandidateSavedListKey;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateAttachmentRepository;
import org.tbbtalent.server.repository.db.CandidateSavedListRepository;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.request.candidate.IHasSetOfSavedLists;
import org.tbbtalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateShareableDocsRequest;
import org.tbbtalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tbbtalent.server.request.list.ContentUpdateType;
import org.tbbtalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFile;

@Service
public class CandidateSavedListServiceImpl implements CandidateSavedListService {
    private final AuthService authService;
    private final CandidateService candidateService;
    private final FileSystemService fileSystemService;
    private final SavedListService savedListService;
    private final SavedListRepository savedListRepository;
    private final UserService userService;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final CandidateSavedListRepository candidateSavedListRepository;
    private static final Logger log = LoggerFactory.getLogger(CandidateSavedListServiceImpl.class);

    public CandidateSavedListServiceImpl(
        AuthService authService, CandidateService candidateService,
        FileSystemService fileSystemService,
        SavedListService savedListService,
        SavedListRepository savedListRepository,
        UserService userService,
        CandidateAttachmentRepository candidateAttachmentRepository,
        CandidateSavedListRepository candidateSavedListRepository) {
        this.authService = authService;
        this.candidateService = candidateService;
        this.fileSystemService = fileSystemService;
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
            removeFromSavedList(candidate, savedList);
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
           removeFromSavedList(candidate, savedList);
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


        //Set any specified Salesforce Job Opportunity
        if (request.getSfJoblink() != null) {
            targetList.setSfJoblink(request.getSfJoblink());
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
        destination.addCandidates(candidates, source);

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
        destination.addCandidates(candidates, sourceList);

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
                removeFromSavedList(candidate, savedList);
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
    public void removeFromSavedList(Candidate candidate, SavedList savedList) {
        final CandidateSavedList csl = new CandidateSavedList(candidate, savedList);
        try {
            candidateSavedListRepository.delete(csl);
            csl.getCandidate().getCandidateSavedLists().remove(csl);
            csl.getSavedList().getCandidateSavedLists().remove(csl);
        } catch (Exception ex) {
            log.warn("Could not delete candidate saved list " + csl.getId(), ex);
        }
    }

    @Override
    public void removeFromSavedList(long savedListId,
        UpdateExplicitSavedListContentsRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
            .orElse(null);
        if (savedList == null) {
            throw new NoSuchObjectException(SavedList.class, savedListId);
        }

        Set<Candidate> candidates = savedListService.fetchCandidates(request);
        for (Candidate candidate : candidates) {
            removeFromSavedList(candidate, savedList);
        }
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
        }

        CandidateAttachment doc = null;
        if (request.getShareableDocAttachmentId() != null) {
            doc = candidateAttachmentRepository.findById(request.getShareableDocAttachmentId())
                .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getShareableDocAttachmentId()));
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
}
