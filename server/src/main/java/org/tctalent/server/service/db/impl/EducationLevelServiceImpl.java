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
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CandidateEducationRepository;
import org.tctalent.server.repository.db.EducationLevelRepository;
import org.tctalent.server.repository.db.EducationLevelSpecification;
import org.tctalent.server.request.education.level.CreateEducationLevelRequest;
import org.tctalent.server.request.education.level.SearchEducationLevelRequest;
import org.tctalent.server.request.education.level.UpdateEducationLevelRequest;
import org.tctalent.server.service.db.EducationLevelService;
import org.tctalent.server.service.db.TranslationService;

@Service
@Slf4j
public class EducationLevelServiceImpl implements EducationLevelService {

    private final CandidateEducationRepository candidateEducationRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final TranslationService translationService;

    @Autowired
    public EducationLevelServiceImpl(CandidateEducationRepository candidateEducationRepository,
                                     EducationLevelRepository educationLevelRepository,
                                     TranslationService translationService) {
        this.candidateEducationRepository = candidateEducationRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.translationService = translationService;
    }

    @Override
    public List<EducationLevel> listEducationLevels() {
        List<EducationLevel> educationLevels = educationLevelRepository.findByStatus(Status.active);
        translationService.translate(educationLevels, "education_level");
        return educationLevels;
    }

    @Override
    public Page<EducationLevel> searchEducationLevels(SearchEducationLevelRequest request) {
        Page<EducationLevel> educationLevels = educationLevelRepository.findAll(
                EducationLevelSpecification.buildSearchQuery(request), request.getPageRequest());
        LogBuilder.builder(log)
            .action("SearchEducationLevels")
            .message("Found " + educationLevels.getTotalElements() + " language levels in search")
            .logInfo();

        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(educationLevels.getContent(), "education_level", request.getLanguage());
        }
        return educationLevels;
    }

    @Override
    public EducationLevel getEducationLevel(long id) {
        return this.educationLevelRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, id));
    }

    @Override
    @Transactional
    public EducationLevel createEducationLevel(CreateEducationLevelRequest request) throws EntityExistsException {
        EducationLevel educationLevel = new EducationLevel(
                request.getName(), request.getStatus(), request.getLevel());
        checkDuplicates(null, request.getName(), request.getLevel());
        return this.educationLevelRepository.save(educationLevel);
    }


    @Override
    @Transactional
    public EducationLevel updateEducationLevel(long id, UpdateEducationLevelRequest request) throws EntityExistsException {
        EducationLevel educationLevel = this.educationLevelRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, id));
        checkDuplicates(id, request.getName(), request.getLevel());

        educationLevel.setName(request.getName());
        educationLevel.setLevel(request.getLevel());
        educationLevel.setStatus(request.getStatus());
        return educationLevelRepository.save(educationLevel);
    }

    @Override
    @Transactional
    public boolean deleteEducationLevel(long id) throws EntityReferencedException {
        EducationLevel educationLevel = educationLevelRepository.findById(id).orElse(null);
        // TO DO ADD EDUCATION LEVEL TO CANDIDATE EDUCATION TABLE
//        List<CandidateEducation> candidateEducations = candidateEducationRepository.findByEducationLevelId(id);
//        if (!Collections.isEmpty(candidateEducations)){
//            throw new EntityReferencedException("education level");
//        }
        if (educationLevel != null) {
            educationLevel.setStatus(Status.deleted);
            educationLevelRepository.save(educationLevel);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name, int level) {
        EducationLevel existingName = educationLevelRepository.findByNameIgnoreCase(name);
        if (existingName != null && !existingName.getId().equals(id) || (existingName != null && id == null)){
            throw new EntityExistsException("education level");
        }

        EducationLevel existingLevel = educationLevelRepository.findByLevelIgnoreCase(level);
        if (existingLevel != null && !existingLevel.getId().equals(id) || (existingLevel != null && id == null)){
            throw new EntityExistsException("education level");
        }
    }
}
