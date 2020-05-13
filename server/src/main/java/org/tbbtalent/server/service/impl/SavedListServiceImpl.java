/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.impl;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.SavedListRepository;
import org.tbbtalent.server.request.list.UpdateSavedListRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.SavedListService;

/**
 * Saved List service
 *
 * @author John Cameron
 */
@Service
public class SavedListServiceImpl implements SavedListService {

    private final CandidateRepository candidateRepository;
    private final SavedListRepository savedListRepository;
    private final UserContext userContext;

    @Autowired
    public SavedListServiceImpl(
            CandidateRepository candidateRepository,
            SavedListRepository savedListRepository,
            UserContext userContext
    ) {
        this.candidateRepository = candidateRepository;
        this.savedListRepository = savedListRepository;
        this.userContext = userContext;
    }
    
    @Override
    @Transactional
    public SavedList createSavedList(UpdateSavedListRequest request) 
            throws EntityExistsException {
        checkDuplicates(null, request.getName());
        SavedList savedList = new SavedList();
        request.populateFromRequest(savedList);

        Set<Candidate> candidates = fetchCandidates(request);
        savedList.addCandidates(candidates);

        return saveIt(savedList);
    }

    @Override
    @Transactional
    public boolean deleteSavedList(long savedListId) {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        if (savedList != null) {

            // Check if user owns this list
            if(savedList.getCreatedBy().getId().equals(userContext.getLoggedInUser().getId())) {

                //Need to clear out many to many relationships before deleting
                //the list otherwise we will have other entities pointing to 
                //this list.
                savedList.setCandidates(null);
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
    public SavedList mergeSavedList(long savedListId, 
                                    UpdateSavedListRequest request) {
        SavedList savedList = this.savedListRepository.findByIdLoadCandidates(savedListId)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, savedListId));
        
        request.populateFromRequest(savedList);
        
        Set<Candidate> candidates = fetchCandidates(request);        
        savedList.addCandidates(candidates);

        return saveIt(savedList);
    }

    @Override
    public SavedList replaceSavedList(long savedListId, 
                                      UpdateSavedListRequest request) {
        SavedList savedList = this.savedListRepository.findByIdLoadCandidates(savedListId)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, savedListId));

        request.populateFromRequest(savedList);

        Set<Candidate> candidates = fetchCandidates(request);
        savedList.addCandidates(candidates);

        return saveIt(savedList);
    }

    // TODO: 1/5/20 This could be common code - or at least the checking 
    private void checkDuplicates(Long id, String name) 
            throws EntityExistsException {
        SavedList existing = savedListRepository.findByNameIgnoreCase(name)
                .orElse(null);
        if (existing != null && existing.getStatus() != Status.deleted) {
            if (!existing.getId().equals(id)) {
                throw new EntityExistsException("SavedList " + existing.getId());
            }
        }
    }

    private @NotNull Set<Candidate> fetchCandidates(UpdateSavedListRequest request) 
            throws NoSuchObjectException {

        Set<Candidate> candidates = new HashSet<>();

        Set<Long> candidateIds = request.getCandidateIds();
        for (Long candidateId : candidateIds) {
            Candidate candidate = candidateRepository.findByUserId(candidateId);
            if (candidate == null) {
                throw new NoSuchObjectException(Candidate.class, candidateId);
            }
            candidates.add(candidate);
        }

        return candidates;
    }

    /**
     * Update audit fields and use repository to save the SavedList
     * @param savedList Entity to save
     * @return Saved entity
     */
    private SavedList saveIt(SavedList savedList) {
        savedList.setAuditFields(userContext.getLoggedInUser());
        return this.savedListRepository.save(savedList);
    }
    
}
