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
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.IndustryRepository;
import org.tbbtalent.server.repository.IndustrySpecification;
import org.tbbtalent.server.request.industry.CreateIndustryRequest;
import org.tbbtalent.server.request.industry.SearchIndustryRequest;
import org.tbbtalent.server.request.industry.UpdateIndustryRequest;
import org.tbbtalent.server.service.IndustryService;

import java.util.List;

@Service
public class IndustryServiceImpl implements IndustryService {

    private static final Logger log = LoggerFactory.getLogger(IndustryServiceImpl.class);

    private final CandidateRepository candidateRepository;
    private final IndustryRepository industryRepository;

    @Autowired
    public IndustryServiceImpl(CandidateRepository candidateRepository, IndustryRepository industryRepository) {
        this.candidateRepository = candidateRepository;
        this.industryRepository = industryRepository;
    }

    @Override
    public List<Industry> listIndustries() { return industryRepository.findByStatus(Status.active); }

    @Override
    public Page<Industry> searchIndustries(SearchIndustryRequest request) {
        Page<Industry> industries = industryRepository.findAll(
                IndustrySpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + industries.getTotalElements() + " industries in search");
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
