package org.tbbtalent.server.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.NationalityRepository;
import org.tbbtalent.server.repository.NationalitySpecification;
import org.tbbtalent.server.request.nationality.CreateNationalityRequest;
import org.tbbtalent.server.request.nationality.SearchNationalityRequest;
import org.tbbtalent.server.request.nationality.UpdateNationalityRequest;
import org.tbbtalent.server.service.NationalityService;

import java.util.List;

@Service
public class NationalityServiceImpl implements NationalityService {

    private static final Logger log = LoggerFactory.getLogger(NationalityServiceImpl.class);

    private final NationalityRepository nationalityRepository;

    @Autowired
    public NationalityServiceImpl(NationalityRepository nationalityRepository) {
        this.nationalityRepository = nationalityRepository;
    }

    @Override
    public List<Nationality> listNationalities() {
        return nationalityRepository.findByStatus(Status.active);
    }

    @Override
    public Page<Nationality> searchNationalities(SearchNationalityRequest request) {
        Page<Nationality> nationalities = nationalityRepository.findAll(
                NationalitySpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + nationalities.getTotalElements() + " nationalities in search");
        return nationalities;
    }

    @Override
    public Nationality getNationality(long id) {
        return this.nationalityRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Nationality.class, id));
    }

    @Override
    @Transactional
    public Nationality createNationality(CreateNationalityRequest request) throws EntityExistsException {
        Nationality nationality = new Nationality(
                request.getName(), Status.active);
        checkDuplicates(null, request.getName());
        return this.nationalityRepository.save(nationality);
    }


    @Override
    @Transactional
    public Nationality updateNationality(long id, UpdateNationalityRequest request) throws EntityExistsException {
        Nationality nationality = this.nationalityRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Nationality.class, id));
        checkDuplicates(id, request.getName());

        nationality.setName(request.getName());
        nationality.setStatus(request.getStatus());
        return nationalityRepository.save(nationality);
    }

    @Override
    @Transactional
    public boolean deleteNationality(long id) {
        Nationality nationality = nationalityRepository.findById(id).orElse(null);
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
