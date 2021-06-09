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

import static org.springframework.data.jpa.domain.Specification.where;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.GetCandidateSavedListsQuery;
import org.tbbtalent.server.repository.db.GetSavedListsQuery;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tbbtalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tbbtalent.server.request.list.ContentUpdateType;
import org.tbbtalent.server.request.list.IHasSetOfCandidates;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tbbtalent.server.request.list.UpdateSavedListContentsRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.SavedListService;

/**
 * Saved List service
 *
 * @author John Cameron
 */
@Service
public class SavedListServiceImpl implements SavedListService {

    private final CandidateRepository candidateRepository;
    private final SavedListRepository savedListRepository;
    private final CandidateSavedListService candidateSavedListService;
    private final SalesforceService salesforceService;
    private final UserRepository userRepository;
    private final UserContext userContext;

    @Autowired
    public SavedListServiceImpl(
        CandidateRepository candidateRepository,
        SavedListRepository savedListRepository,
        CandidateSavedListService candidateSavedListService,
        SalesforceService salesforceService, UserRepository userRepository,
        UserContext userContext
    ) {
        this.candidateRepository = candidateRepository;
        this.savedListRepository = savedListRepository;
        this.candidateSavedListService = candidateSavedListService;
        this.salesforceService = salesforceService;
        this.userRepository = userRepository;
        this.userContext = userContext;
    }

    @Override
    public boolean clearSavedList(long savedListId) throws InvalidRequestException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        boolean done = true;
        if (savedList == null) {
            done = false;
        } else {
            candidateSavedListService.clearSavedListCandidates(savedList);
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
            targetList = createSavedList(request);
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

        saveIt(destination);
    }

    @Override
    public void copyContents(UpdateExplicitSavedListContentsRequest request,
        SavedList destination) {
        
        if (request.getUpdateType() == ContentUpdateType.replace) {
            clearSavedList(destination.getId());
        }

        //Retrieve source list, if any
        SavedList sourceList = fetchSourceList(request);

        //New list inherits source's savedSearchSource, if any
        if (sourceList != null) {
            final SavedSearch savedSearchSource = sourceList.getSavedSearchSource();
            if (savedSearchSource != null) {
                destination.setSavedSearchSource(savedSearchSource);
            }
        }

        //Retrieve candidates
        Set<Candidate> candidates = fetchCandidates(request);

        //Add candidates to created list, together with any context if source 
        //list was supplied.
        destination.addCandidates(candidates, sourceList);

        saveIt(destination);
    }

    @Override
    @Transactional
    public SavedList createSavedList(UpdateSavedListInfoRequest request) 
            throws EntityExistsException {
        final User loggedInUser = userContext.getLoggedInUser().orElse(null);
        if (loggedInUser != null) {
            checkDuplicates(null, request.getName(), loggedInUser.getId());
        }
        SavedList savedList = new SavedList();
        request.populateFromRequest(savedList);
        
        //Save created list so that we get its id from the database
        return saveIt(savedList);
    }

    @Override
    @Transactional
    public boolean deleteSavedList(long savedListId) {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        final User loggedInUser = userContext.getLoggedInUser().orElse(null);
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
    public SavedList get(long savedListId) {
        return savedListRepository.findById(savedListId)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, savedListId));
    }

    @Override
    public void mergeSavedList(long savedListId,
        UpdateExplicitSavedListContentsRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);
        if (savedList == null) {
            throw new NoSuchObjectException(SavedList.class, savedListId);
        }

        SavedList sourceList = fetchSourceList(request);
        Set<Candidate> candidates = fetchCandidates(request);
        savedList.addCandidates(candidates, sourceList);

        saveIt(savedList);
    }

    @Override
    public void mergeSavedListFromFile(long savedListId, MultipartFile file)
        throws NoSuchObjectException, IOException {

        Set<Long> candidateIds = new HashSet<>();

        //Extract candidate ids from file
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        try {
            while (reader.ready()) {
                String[] tokens = reader.readLine().split("\\s*,\\s*");
                if (tokens.length > 0) {
                    Long candidateId = Long.parseLong(tokens[0]);
                    Candidate candidate = candidateRepository.findById(candidateId).orElse(null);
                    if (candidate == null) {
                        throw new NoSuchObjectException(Candidate.class, candidateId);
                    }
                    candidateIds.add(candidateId);
                }
            }
        } catch (NumberFormatException ex) {
            throw new NoSuchObjectException("Non numeric candidate id " + ex.getMessage());
        }

        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
        request.setCandidateIds(candidateIds);
        request.setUpdateType(ContentUpdateType.add);
        mergeSavedList(savedListId, request);
    }

    @Override
    public void removeFromSavedList(long savedListId, 
        UpdateExplicitSavedListContentsRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);
        if (savedList == null) {
            throw new NoSuchObjectException(SavedList.class, savedListId);
        }

        Set<Candidate> candidates = fetchCandidates(request);
        for (Candidate candidate : candidates) {
            candidateSavedListService.removeFromSavedList(candidate, savedList);
        }
    }

    @Override
    public List<SavedList> search(long candidateId, SearchSavedListRequest request) {
        final User loggedInUser = userContext.getLoggedInUser().orElse(null);
        User userWithSharedSearches = loggedInUser == null ? null :
                userRepository.findByIdLoadSharedSearches(loggedInUser.getId());
        GetSavedListsQuery getSavedListsQuery =
                new GetSavedListsQuery(request, userWithSharedSearches);

        GetCandidateSavedListsQuery getCandidateSavedListsQuery = 
                new GetCandidateSavedListsQuery(candidateId);
        
        //Set standard sort to ascending by name.
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return savedListRepository.findAll( 
                where(getSavedListsQuery).and(getCandidateSavedListsQuery), 
                sort);
    }

    @Override
    public List<SavedList> listSavedLists(SearchSavedListRequest request) {
        final User loggedInUser = userContext.getLoggedInUser().orElse(null);
        User userWithSharedSearches = loggedInUser == null ? null :
                userRepository.findByIdLoadSharedSearches(
                        loggedInUser.getId());
        GetSavedListsQuery getSavedListsQuery = 
                new GetSavedListsQuery(request, userWithSharedSearches);
        
        //The request is not required to provide paging or sorting info and
        //we ignore any such info if present because we don't pass a PageRequest
        //to the repository findAll call.
        //But set standard sort to ascending by name.
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return savedListRepository.findAll(getSavedListsQuery, sort);
    }

    @Override
    public Page<SavedList> searchSavedLists(SearchSavedListRequest request) {
        final User loggedInUser = userContext.getLoggedInUser().orElse(null);
        User userWithSharedSearches = loggedInUser == null ? null :
                userRepository.findByIdLoadSharedSearches(
                        loggedInUser.getId());
        GetSavedListsQuery getSavedListsQuery = 
                new GetSavedListsQuery(request, userWithSharedSearches);
        
        //The incoming request will have paging info but no sorting.
        //So set standard ascending sort by name.
        request.setSortDirection(Sort.Direction.ASC);
        request.setSortFields(new String[] {"name"});
        
        PageRequest pageRequest = request.getPageRequest();
        return savedListRepository.findAll(getSavedListsQuery, pageRequest);
    }

    @Override
    public SavedList updateSavedList(long savedListId, UpdateSavedListInfoRequest request) 
            throws NoSuchObjectException, EntityExistsException {
        final User loggedInUser = userContext.getLoggedInUser().orElse(null);
        if (loggedInUser != null) {
            checkDuplicates(savedListId, request.getName(), loggedInUser.getId());
        }
        SavedList savedList = get(savedListId);
        request.populateFromRequest(savedList);
        return saveIt(savedList);
    }

    @Override
    public void updateDisplayedFieldPaths(
            long savedListId, UpdateDisplayedFieldPathsRequest request) 
            throws NoSuchObjectException {
        SavedList savedList = get(savedListId);
        if (request.getDisplayedFieldsLong() != null) {
            savedList.setDisplayedFieldsLong(request.getDisplayedFieldsLong());
        }
        if (request.getDisplayedFieldsShort() != null) {
            savedList.setDisplayedFieldsShort(request.getDisplayedFieldsShort());
        }
        saveIt(savedList);
    }

    @Override
    @Transactional
    public SavedList addSharedUser(long id, UpdateSharingRequest request) 
            throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));

        final Long userID = request.getUserId();
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new NoSuchObjectException(User.class, userID));

        savedList.addUser(user);

        return savedListRepository.save(savedList);
    }

    @Override
    @Transactional
    public SavedList removeSharedUser(long id, UpdateSharingRequest request) 
            throws NoSuchObjectException {
        SavedList savedList = savedListRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));

        final Long userID = request.getUserId();
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new NoSuchObjectException(User.class, userID));

        savedList.removeUser(user);

        return savedListRepository.save(savedList);
    }

    private void checkDuplicates(Long savedListId, String name, Long userId) 
            throws EntityExistsException {
        SavedList existing = savedListRepository.findByNameIgnoreCase(name, userId)
                .orElse(null);
        if (existing != null && existing.getStatus() != Status.deleted) {
            if (!existing.getId().equals(savedListId)) {
                throw new EntityExistsException("SavedList " + existing.getId());
            }
        }
    }

    private @NotNull Set<Candidate> fetchCandidates(IHasSetOfCandidates request) 
            throws NoSuchObjectException {

        Set<Candidate> candidates = new HashSet<>();

        Set<Long> candidateIds = request.getCandidateIds();
        if (candidateIds != null) {
            for (Long candidateId : candidateIds) {
                Candidate candidate = candidateRepository.findById(candidateId)
                        .orElse(null);
                if (candidate == null) {
                    throw new NoSuchObjectException(Candidate.class, candidateId);
                }
                candidates.add(candidate);
            }
        }

        return candidates;
    }

    @Nullable
    private SavedList fetchSourceList(UpdateSavedListContentsRequest request) 
            throws NoSuchObjectException {
        SavedList sourceList = null;
        Long sourceListId = request.getSourceListId();
        if (sourceListId != null) {
            sourceList = savedListRepository.findByIdLoadCandidates(sourceListId)
                    .orElseThrow(() -> new NoSuchObjectException(SavedList.class, sourceListId));
        }
        return sourceList;
    }

    /**
     * Update audit fields and use repository to save the SavedList
     * @param savedList Entity to save
     * @return Saved entity
     */
    private SavedList saveIt(SavedList savedList) {
        savedList.setAuditFields(userContext.getLoggedInUser().orElse(null));
        return savedListRepository.save(savedList);
    }
    
}
