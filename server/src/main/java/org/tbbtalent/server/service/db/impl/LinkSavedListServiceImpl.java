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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.LinkSavedList;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.repository.db.LinkSavedListRepository;
import org.tbbtalent.server.repository.db.LinkSavedListSpecification;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.request.link.CreateLinkRequest;
import org.tbbtalent.server.request.link.SearchLinkRequest;
import org.tbbtalent.server.request.link.UpdateLinkRequest;
import org.tbbtalent.server.service.db.LinkSavedListService;
import org.tbbtalent.server.service.db.OccupationService;

import java.util.List;

@Service
public class LinkSavedListServiceImpl implements LinkSavedListService {
    private static final Logger log = LoggerFactory.getLogger(OccupationService.class);

    private final LinkSavedListRepository linkSavedListRepository;
    private final SavedListRepository savedListRepository;

    @Autowired
    public LinkSavedListServiceImpl(LinkSavedListRepository linkSavedListRepository,
                                    SavedListRepository savedListRepository) {
        this.linkSavedListRepository = linkSavedListRepository;
        this.savedListRepository = savedListRepository;
    }

    @Override
    public List<LinkSavedList> listLinks() {
        List<LinkSavedList> links = linkSavedListRepository.findAll();
        return links;
    }

    @Override
    public Page<LinkSavedList> searchLinks(SearchLinkRequest request) {
        Page<LinkSavedList> occupations = linkSavedListRepository.findAll(LinkSavedListSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + occupations.getTotalElements() + " occupations in search");
        return occupations;
    }

    @Override
    public LinkSavedList getLink(long id) {
        return this.linkSavedListRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, id));
    }

    @Override
    @Transactional
    public LinkSavedList createLink(CreateLinkRequest request) throws EntityExistsException {
        LinkSavedList link = new LinkSavedList();
        SavedList savedList = this.savedListRepository.findById(request.getSavedListId())
                .orElseThrow(() -> new NoSuchObjectException(SavedList.class, request.getSavedListId()));
        link.setSavedList(savedList);
        link.setLink(request.getLink());
        //checkDuplicates(null, request.getName());
        return this.linkSavedListRepository.save(link);
    }


    @Override
    @Transactional
    public LinkSavedList updateLink(long id, UpdateLinkRequest request) throws EntityExistsException {
        LinkSavedList link = this.linkSavedListRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, id));
        //checkDuplicates(id, request.getName());

        link.setLink(request.getLink());
        return linkSavedListRepository.save(link);
    }

    @Override
    @Transactional
    public boolean deleteLink(long id) throws EntityReferencedException {
        LinkSavedList link = linkSavedListRepository.findById(id).orElse(null);
        return true;
    }

    private void checkDuplicates(Long id, String name) {
//        LinkSavedList existing = LinkSavedListRepository.findByNameIgnoreCase(name);
//        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
//            throw new EntityExistsException("occupation");
//        }
    }
}
