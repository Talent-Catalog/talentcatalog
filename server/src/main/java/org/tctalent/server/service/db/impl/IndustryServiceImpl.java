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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.IndustryRepository;
import org.tctalent.server.repository.db.IndustrySpecification;
import org.tctalent.server.request.industry.CreateIndustryRequest;
import org.tctalent.server.request.industry.SearchIndustryRequest;
import org.tctalent.server.request.industry.UpdateIndustryRequest;
import org.tctalent.server.service.db.IndustryService;
import org.tctalent.server.service.db.TranslationService;

@Service
@Slf4j
public class IndustryServiceImpl implements IndustryService {

    private final CandidateRepository candidateRepository;
    private final IndustryRepository industryRepository;
    private final TranslationService translationService;

    @Autowired
    public IndustryServiceImpl(CandidateRepository candidateRepository,
                               IndustryRepository industryRepository,
                               TranslationService translationService) {
        this.candidateRepository = candidateRepository;
        this.industryRepository = industryRepository;
        this.translationService = translationService;
    }

    @Override
    public List<Industry> listIndustries() {
        List<Industry> industries = industryRepository.findByStatus(Status.active);
        translationService.translate(industries, "industry");
        return industries;
    }

    @Override
    public Page<Industry> searchIndustries(SearchIndustryRequest request) {
        Page<Industry> industries = industryRepository.findAll(
                IndustrySpecification.buildSearchQuery(request), request.getPageRequest());
        LogBuilder.builder(log)
            .action("SearchIndustries")
            .message("Found " + industries.getTotalElements() + " industries in search")
            .logInfo();

        return industries;
    }

    @Override
    public Industry getIndustry(long id) {
        return this.industryRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Industry.class, id));
    }

    @Override
    @Transactional
    public Industry createIndustry(CreateIndustryRequest request) throws EntityExistsException {
        Industry industry = new Industry(
                request.getName(), request.getStatus());
        checkDuplicates(null, request.getName());
        return this.industryRepository.save(industry);
    }


    @Override
    @Transactional
    public Industry updateIndustry(long id, UpdateIndustryRequest request) throws EntityExistsException {
        Industry industry = this.industryRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Industry.class, id));
        checkDuplicates(id, request.getName());

        industry.setName(request.getName());
        industry.setStatus(request.getStatus());
        return industryRepository.save(industry);
    }

    @Override
    @Transactional
    public boolean deleteIndustry(long id) throws EntityReferencedException {
        Industry industry = industryRepository.findById(id).orElse(null);
        // TO DO - FIND WHERE INDUSTRY LINKS IN THE DATABASE
//        List<Candidate> candidates = candidateRepository.findByNationalityId(id);
//        if (!Collections.isEmpty(candidates)){
//            throw new EntityReferencedException("industry");
//        }
        if (industry != null) {
            industry.setStatus(Status.deleted);
            industryRepository.save(industry);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name) {
        Industry existing = industryRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("industry");
        }
    }


}
