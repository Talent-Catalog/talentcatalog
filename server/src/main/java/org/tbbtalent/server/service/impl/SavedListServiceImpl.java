/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.SavedListRepository;
import org.tbbtalent.server.request.list.UpdateSavedListRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.SavedListService;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
public class SavedListServiceImpl implements SavedListService {

    private final SavedListRepository savedListRepository;
    private final UserContext userContext;

    @Autowired
    public SavedListServiceImpl(
            SavedListRepository savedListRepository,
            UserContext userContext
    ) {
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

        // TODO: 1/5/20 Could this be hidden away in a helper class 
        savedList.setAuditFields(userContext.getLoggedInUser());
        savedList = this.savedListRepository.save(savedList);
        return savedList;
    }

    // TODO: 1/5/20 This could be common code - or at least the checking 
    private void checkDuplicates(Long id, String name) 
            throws EntityExistsException {
        SavedList existing = savedListRepository.findByNameIgnoreCase(name);
        if (existing != null && existing.getStatus() != Status.deleted) {
            if (!existing.getId().equals(id)) {
                throw new EntityExistsException("SavedList " + existing.getId());
            }
        }
    }
    
}
