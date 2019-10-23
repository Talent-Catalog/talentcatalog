package org.tbbtalent.server.service.impl;

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
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateEducationRepository;
import org.tbbtalent.server.repository.EducationMajorRepository;
import org.tbbtalent.server.repository.EducationMajorSpecification;
import org.tbbtalent.server.request.education.major.CreateEducationMajorRequest;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;
import org.tbbtalent.server.request.education.major.UpdateEducationMajorRequest;
import org.tbbtalent.server.service.EducationMajorService;
import org.tbbtalent.server.service.TranslationService;

import java.util.List;

@Service
public class EducationMajorServiceImpl implements EducationMajorService {

    private static final Logger log = LoggerFactory.getLogger(LanguageLevelServiceImpl.class);

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
        log.info("Found " + educationMajors.getTotalElements() + " education majors in search");
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
