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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.Nationality;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.NationalityRepository;
import org.tbbtalent.server.repository.db.NationalitySpecification;
import org.tbbtalent.server.request.nationality.CreateNationalityRequest;
import org.tbbtalent.server.request.nationality.SearchNationalityRequest;
import org.tbbtalent.server.request.nationality.UpdateNationalityRequest;
import org.tbbtalent.server.service.db.NationalityService;
import org.tbbtalent.server.service.db.TranslationService;

import io.jsonwebtoken.lang.Collections;

@Service
public class NationalityServiceImpl implements NationalityService {

    private static final Logger log = LoggerFactory.getLogger(NationalityServiceImpl.class);

    private Map<Long, Nationality> cache = null;
    private final CandidateRepository candidateRepository;
    private final NationalityRepository nationalityRepository;
    private final TranslationService translationService;

    @Autowired
    public NationalityServiceImpl(CandidateRepository candidateRepository,
                                  NationalityRepository nationalityRepository,
                                  TranslationService translationService) {
        this.candidateRepository = candidateRepository;
        this.nationalityRepository = nationalityRepository;
        this.translationService = translationService;
    }

    private void dropCache() {
        cache = null;
    }

    private void loadCache() {
        if (cache == null) {
            cache = new HashMap<>();
            List<Nationality> nationalities = nationalityRepository.findAll();
            for (Nationality nationality : nationalities) {
                cache.put(nationality.getId(), nationality);
            }
        }
    }

    @Override
    public List<Nationality> listNationalities() {
        //Note: Can't use cache because translationService modifies it adding 
        //translations - which will always get returned to user (because of
        //the way Dto builder works - if translation is present, it will use 
        //that as name).
        List<Nationality> nationalities = nationalityRepository.findByStatus(Status.active);
        translationService.translate(nationalities, "nationality");
        return nationalities;
    }

    @Override
    public Page<Nationality> searchNationalities(SearchNationalityRequest request) {
        Page<Nationality> nationalities = nationalityRepository.findAll(
                NationalitySpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + nationalities.getTotalElements() + " nationalities in search");
        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(nationalities.getContent(), "nationality", request.getLanguage());
        }
        return nationalities;
    }

    @Override
    public Nationality getNationality(long id) {
        loadCache();
        Nationality nationality = cache.get(id);
        if (nationality == null) {
            throw new NoSuchObjectException(Nationality.class, id);
        }
        return nationality;
    }

    @Override
    @Transactional
    public Nationality createNationality(CreateNationalityRequest request) throws EntityExistsException {
        dropCache();
        Nationality nationality = new Nationality(
                request.getName(), request.getStatus());
        checkDuplicates(null, request.getName());
        return this.nationalityRepository.save(nationality);
    }


    @Override
    @Transactional
    public Nationality updateNationality(long id, UpdateNationalityRequest request) throws EntityExistsException {
        dropCache();
        Nationality nationality = this.nationalityRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Nationality.class, id));
        checkDuplicates(id, request.getName());

        nationality.setName(request.getName());
        nationality.setStatus(request.getStatus());
        return nationalityRepository.save(nationality);
    }

    @Override
    @Transactional
    public boolean deleteNationality(long id) throws EntityReferencedException {
        dropCache();
        Nationality nationality = nationalityRepository.findById(id).orElse(null);
        List<Candidate> candidates = candidateRepository.findByNationalityId(id);
        if (!Collections.isEmpty(candidates)){
            throw new EntityReferencedException("nationality");
        }
        if (nationality != null) {
            nationality.setStatus(Status.deleted);
            nationalityRepository.save(nationality);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name) {
        Nationality existing = nationalityRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("nationality");
        }
    }


}
