package org.tbbtalent.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateEducationRepository;
import org.tbbtalent.server.repository.EducationLevelRepository;
import org.tbbtalent.server.repository.EducationLevelSpecification;
import org.tbbtalent.server.request.education.level.CreateEducationLevelRequest;
import org.tbbtalent.server.request.education.level.SearchEducationLevelRequest;
import org.tbbtalent.server.request.education.level.UpdateEducationLevelRequest;
import org.tbbtalent.server.service.EducationLevelService;

import java.util.List;

@Service
public class EducationLevelServiceImpl implements EducationLevelService {

    private static final Logger log = LoggerFactory.getLogger(LanguageLevelServiceImpl.class);

    private final CandidateEducationRepository candidateEducationRepository;
    private final EducationLevelRepository educationLevelRepository;

    @Autowired
    public EducationLevelServiceImpl(CandidateEducationRepository candidateEducationRepository, EducationLevelRepository educationLevelRepository) {
        this.candidateEducationRepository = candidateEducationRepository;
        this.educationLevelRepository = educationLevelRepository;
    }

    @Override
    public List<EducationLevel> listEducationLevels() {
        return educationLevelRepository.findByStatus(Status.active);
    }

    @Override
    public Page<EducationLevel> searchEducationLevels(SearchEducationLevelRequest request) {
        Page<EducationLevel> educationLevels = educationLevelRepository.findAll(
                EducationLevelSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + educationLevels.getTotalElements() + " language levels in search");
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
        checkDuplicates(null, request.getName());
        return this.educationLevelRepository.save(educationLevel);
    }


    @Override
    @Transactional
    public EducationLevel updateEducationLevel(long id, UpdateEducationLevelRequest request) throws EntityExistsException {
        EducationLevel educationLevel = this.educationLevelRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, id));
        checkDuplicates(id, request.getName());

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

    private void checkDuplicates(Long id, String name) {
        EducationLevel existing = educationLevelRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("country");
        }
    }
}
