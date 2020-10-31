/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.GetCandidateSavedListsQuery;
import org.tbbtalent.server.repository.db.GetSavedListsQuery;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.request.list.CreateSavedListRequest;
import org.tbbtalent.server.request.list.IHasSetOfCandidates;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.request.list.TargetListSelection;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.SavedListService;

import static org.springframework.data.jpa.domain.Specification.where;

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
    public SavedList copy(long id, TargetListSelection request) 
            throws EntityExistsException, NoSuchObjectException {
        SavedList sourceList = savedListRepository.findByIdLoadCandidates(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));

        SavedList targetList;
        final Long targetId = request.getSavedListId();
        if (targetId == 0) {
            //Request is to create a new list
            CreateSavedListRequest createRequest = new CreateSavedListRequest();
            createRequest.setName(request.getNewListName());
            createRequest.setFixed(false);
            targetList = createSavedList(createRequest);
        } else {
            targetList = savedListRepository.findByIdLoadCandidates(targetId)
                    .orElseThrow(() -> new NoSuchObjectException(SavedList.class, targetId));
        }


        //Set any specified Salesforce Job Opportunity
        if (request.getSfJoblink() != null) {
            targetList.setSfJoblink(request.getSfJoblink());
        }

        //Copy across list contents (which includes context notes)
        copyContents(sourceList, targetList, request.isReplace());

        return targetList;
    }

    @Override
    public void copyContents(
            SavedList source, SavedList destination, boolean replace) {
        //Get candidates in source list
        final Set<Candidate> candidates = source.getCandidates();

        //Add or replace them to desintation as requested.
        if (replace) {
            clearSavedList(destination.getId());
        }
        destination.addCandidates(candidates, source);

        saveIt(destination);
    }

    @Override
    @Transactional
    public SavedList createSavedList(CreateSavedListRequest request) 
            throws EntityExistsException {
        final User loggedInUser = userContext.getLoggedInUser();
        if (loggedInUser != null) {
            checkDuplicates(null, request.getName(), loggedInUser.getId());
        }
        SavedList savedList = new SavedList();
        request.populateFromRequest(savedList);
        
        //Save created list so that we get its id from the database
        savedList = saveIt(savedList);

        //Retrieve source list, if any
        SavedList sourceList = fetchSourceList(request);

        //Retrieve candidates
        Set<Candidate> candidates = fetchCandidates(request);

        //Add candidates to created list, together with any context if source 
        //list was supplied.
        savedList.addCandidates(candidates, sourceList);

        return saveIt(savedList);
    }

    @Override
    @Transactional
    public boolean deleteSavedList(long savedListId) {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        final User loggedInUser = userContext.getLoggedInUser();
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
    public boolean mergeSavedList(long savedListId, 
                                    IHasSetOfCandidates request) {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        boolean done = true;
        if (savedList == null) {
            done = false;
        } else {
            SavedList sourceList = fetchSourceList(request);
            Set<Candidate> candidates = fetchCandidates(request);
            savedList.addCandidates(candidates, sourceList);

            saveIt(savedList);
        }
        return done;
    }

    @Override
    public boolean removeFromSavedList(long savedListId, 
                                    IHasSetOfCandidates request) {
        SavedList savedList = savedListRepository.findByIdLoadCandidates(savedListId)
                .orElse(null);

        boolean done = true;
        if (savedList == null) {
            done = false;
        } else {
            Set<Candidate> candidates = fetchCandidates(request);
            for (Candidate candidate : candidates) {
                candidateSavedListService.removeFromSavedList(candidate, savedList);
            }
        }
        return done;
    }

    @Override
    public List<SavedList> search(long candidateId, SearchSavedListRequest request) {
        final User loggedInUser = userContext.getLoggedInUser();
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
        final User loggedInUser = userContext.getLoggedInUser();
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
        final User loggedInUser = userContext.getLoggedInUser();
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
        final User loggedInUser = userContext.getLoggedInUser();
        if (loggedInUser != null) {
            checkDuplicates(savedListId, request.getName(), loggedInUser.getId());
        }
        SavedList savedList = get(savedListId);
        request.populateFromRequest(savedList);
        return saveIt(savedList);
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

    @Override
    public void createUpdateSalesforce(long id) 
            throws NoSuchObjectException, GeneralSecurityException, WebClientException {
        SavedList savedList = savedListRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, id));

        List<Candidate> candidates = new ArrayList<>(savedList.getCandidates());
        List<Contact> contacts = 
                salesforceService.createOrUpdateContacts(candidates);

        //Update the sfLink in all candidate records.
        int nCandidates = candidates.size();
        for (int i = 0; i < nCandidates; i++) {
            Contact contact = contacts.get(i);
            if (contact.getId() != null) {
                Candidate candidate = candidates.get(i);
                candidate.setSflink(contact.getUrl());
                candidateRepository.save(candidate);
            }
        }
        
        String sfJoblink = savedList.getSfJoblink();
        if (sfJoblink != null && sfJoblink.length() > 0) {
            salesforceService.createOrUpdateJobOpportunities(
                    candidates, sfJoblink);
        }
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
    private SavedList fetchSourceList(IHasSetOfCandidates request) 
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
        savedList.setAuditFields(userContext.getLoggedInUser());
        return savedListRepository.save(savedList);
    }
    
}
