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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.model.db.SavedListLink;
import org.tctalent.server.repository.db.SavedListLinkRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.service.db.SavedListLinkService;

@Service
@Slf4j
public class SavedListLinkServiceImpl implements SavedListLinkService {

    private final SavedListLinkRepository savedListLinkRepository;
    private final SavedListRepository savedListRepository;

    @Autowired
    public SavedListLinkServiceImpl(SavedListLinkRepository savedListLinkRepository,
                                    SavedListRepository savedListRepository) {
        this.savedListLinkRepository = savedListLinkRepository;
        this.savedListRepository = savedListRepository;
    }

//    @Override
//    public List<SavedListLink> listLinks() {
//        List<SavedListLink> links = savedListLinkRepository.findAll();
//        return links;
//    }
//
//    @Override
//    public Page<SavedListLink> searchLinks(SearchLinkRequest request) {
//        Page<SavedListLink> occupations = savedListLinkRepository.findAll(SavedListLinkSpecification.buildSearchQuery(request), request.getPageRequest());
//        log.info("Found " + occupations.getTotalElements() + " occupations in search");
//        return occupations;
//    }
//
//    @Override
//    public SavedListLink getLink(long id) {
//        return this.savedListLinkRepository.findById(id)
//                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, id));
//    }

//    @Override
//    @Transactional
//    public SavedListLink createLink(CreateLinkRequest request) throws EntityExistsException {
//        SavedListLink link = new SavedListLink();
//        SavedList savedList = this.savedListRepository.findById(request.getSavedListId())
//                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, request.getSavedListId()));
//
//        checkDuplicates(null, request.getSavedListId(), request.getLink());
//
//        link.setSavedList(savedList);
//        link.setLink(request.getLink());
//        return this.savedListLinkRepository.save(link);
//    }
//
//
//    @Override
//    @Transactional
//    public SavedListLink updateLink(long id, UpdateShortNameRequest request) throws EntityExistsException {
//        SavedListLink link = this.savedListLinkRepository.findById(id)
//                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, id));
//
//        checkDuplicates(id, request.getSavedListId(), request.getTbbShortName());
//
//        link.setLink(request.getTbbShortName());
//        return savedListLinkRepository.save(link);
//    }
//
//    @Override
//    @Transactional
//    public boolean deleteLink(long id) throws EntityReferencedException {
//        this.savedListLinkRepository.deleteById(id);
//        return true;
//    }

    private void checkDuplicates(Long id, Long savedListId, String link) {
        // Can't have the same link name
        SavedListLink existingLink = this.savedListLinkRepository.findByLinkIgnoreCase(link);

        if (existingLink != null && !existingLink.getId().equals(id)) {
            throw new EntityExistsException("external link");
        }

        // Can't have two of the same saved lists
        SavedListLink savedListLink = this.savedListLinkRepository.findBySavedList(savedListId);
        // If the requested saved list already has a link, and it doesn't belong to the record which is being updated.
        if (savedListLink != null && !savedListLink.getId().equals(id)){
            throw new EntityExistsException("link for a saved list");
        }
    }
}
