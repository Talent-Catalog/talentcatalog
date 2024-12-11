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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CandidateEducationRepository;
import org.tctalent.server.repository.db.EducationMajorRepository;
import org.tctalent.server.repository.db.EducationMajorSpecification;
import org.tctalent.server.request.education.major.CreateEducationMajorRequest;
import org.tctalent.server.request.education.major.SearchEducationMajorRequest;
import org.tctalent.server.request.education.major.UpdateEducationMajorRequest;
import org.tctalent.server.service.db.EducationMajorService;
import org.tctalent.server.service.db.TranslationService;

@Service
@Slf4j
public class EducationMajorServiceImpl implements EducationMajorService {

    private final CandidateEducationRepository candidateEducationRepository;
    private final EducationMajorRepository educationMajorRepository;
    private final TranslationService translationService;

    @Autowired
    public EducationMajorServiceImpl(CandidateEducationRepository candidateEducationRepository,
                                     EducationMajorRepository educationMajorRepository,
                                     TranslationService translationService) {
        this.candidateEducationRepository = candidateEducationRepository;
        this.educationMajorRepository = educationMajorRepository;
        this.translationService = translationService;
    }

    @Override
    public List<EducationMajor> listActiveEducationMajors() {
        List<EducationMajor> majors = educationMajorRepository.findByStatus(Status.active);
        translationService.translate(majors, "education_major");
        return majors;
    }

    @Override
    public Page<EducationMajor> searchEducationMajors(SearchEducationMajorRequest request) {
        Page<EducationMajor> educationMajors = educationMajorRepository.findAll(
                EducationMajorSpecification.buildSearchQuery(request), request.getPageRequest());
        LogBuilder.builder(log)
            .action("SearchEducationMajors")
            .message("Found " + educationMajors.getTotalElements() + " education majors in search")
            .logInfo();

        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(educationMajors.getContent(), "education_major", request.getLanguage());
        }
        return educationMajors;
    }

    @Override
    public EducationMajor getEducationMajor(long id) {
        return this.educationMajorRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(EducationMajor.class, id));
    }

    @Override
    @Transactional
    public EducationMajor createEducationMajor(CreateEducationMajorRequest request) throws EntityExistsException {
        EducationMajor educationMajor = new EducationMajor(
                request.getName(), request.getStatus());
        checkDuplicates(null, request.getName());
        return this.educationMajorRepository.save(educationMajor);
    }


    @Override
    @Transactional
    public EducationMajor updateEducationMajor(long id, UpdateEducationMajorRequest request) throws EntityExistsException {
        EducationMajor educationMajor = this.educationMajorRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(EducationMajor.class, id));
        checkDuplicates(id, request.getName());

        educationMajor.setName(request.getName());
        educationMajor.setStatus(request.getStatus());
        return educationMajorRepository.save(educationMajor);
    }

    @Override
    @Transactional
    public boolean deleteEducationMajor(long id) throws EntityReferencedException {
        EducationMajor educationMajor = educationMajorRepository.findById(id).orElse(null);
        // TO DO ADD EDUCATION MAJOR TO CANDIDATE EDUCATION TABLE
//        List<CandidateEducation> candidateEducations = candidateEducationRepository.findByEducationMajorId(id);
//        if (!Collections.isEmpty(candidateEducations)){
//            throw new EntityReferencedException("education major");
//        }
        if (educationMajor != null) {
            educationMajor.setStatus(Status.deleted);
            educationMajorRepository.save(educationMajor);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name) {
        EducationMajor existing = educationMajorRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("educationMajor");
        }
    }

}
