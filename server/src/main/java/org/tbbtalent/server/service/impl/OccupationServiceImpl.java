package org.tbbtalent.server.service.impl;

import io.jsonwebtoken.lang.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateOccupationRepository;
import org.tbbtalent.server.repository.OccupationRepository;
import org.tbbtalent.server.repository.OccupationSpecification;
import org.tbbtalent.server.request.occupation.CreateOccupationRequest;
import org.tbbtalent.server.request.occupation.SearchOccupationRequest;
import org.tbbtalent.server.request.occupation.UpdateOccupationRequest;
import org.tbbtalent.server.service.OccupationService;
import org.tbbtalent.server.service.TranslationService;

import java.util.List;

@Service
public class OccupationServiceImpl implements OccupationService {

    private static final Logger log = LoggerFactory.getLogger(OccupationService.class);

    private final CandidateOccupationRepository candidateOccupationRepository;
    private final OccupationRepository occupationRepository;
    private final TranslationService translationService;

    @Autowired
    public OccupationServiceImpl(CandidateOccupationRepository candidateOccupationRepository,
                                 OccupationRepository occupationRepository,
                                 TranslationService translationService) {
        this.candidateOccupationRepository = candidateOccupationRepository;
        this.occupationRepository = occupationRepository;
        this.translationService = translationService;
    }

    @Override
    public List<Occupation> listOccupations() {
        List<Occupation> occupations = occupationRepository.findByStatus(Status.active);
        translationService.translate(occupations, "occupation");
        return occupations;
    }

    @Override
    public Page<Occupation> searchOccupations(SearchOccupationRequest request) {
        Page<Occupation> occupations = occupationRepository.findAll(
                OccupationSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + occupations.getTotalElements() + " occupations in search");
        return occupations;
    }

    @Override
    public Occupation getOccupation(long id) {
        return this.occupationRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, id));
    }

    @Override
    @Transactional
    public Occupation createOccupation(CreateOccupationRequest request) throws EntityExistsException {
        Occupation occupation = new Occupation(
                request.getName(), request.getStatus());
        checkDuplicates(null, request.getName());
        return this.occupationRepository.save(occupation);
    }


    @Override
    @Transactional
    public Occupation updateOccupation(long id, UpdateOccupationRequest request) throws EntityExistsException {
        Occupation occupation = this.occupationRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, id));
        checkDuplicates(id, request.getName());

        occupation.setName(request.getName());
        occupation.setStatus(request.getStatus());
        return occupationRepository.save(occupation);
    }

    @Override
    @Transactional
    public boolean deleteOccupation(long id) throws EntityReferencedException {
        Occupation occupation = occupationRepository.findById(id).orElse(null);
        List<CandidateOccupation> candidateOccupations = candidateOccupationRepository.findByOccupationId(id);
        if (!Collections.isEmpty(candidateOccupations)){
            throw new EntityReferencedException("occupation");
        }
        if (occupation != null) {
            occupation.setStatus(Status.deleted);
            occupationRepository.save(occupation);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name) {
        Occupation existing = occupationRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("occupation");
        }
    }


}
