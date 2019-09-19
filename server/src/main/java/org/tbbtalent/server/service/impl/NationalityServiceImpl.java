package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.repository.NationalityRepository;
import org.tbbtalent.server.service.NationalityService;

import java.util.List;

@Service
public class NationalityServiceImpl implements NationalityService {

    private final NationalityRepository nationalityRepository;

    @Autowired
    public NationalityServiceImpl(NationalityRepository nationalityRepository) {
        this.nationalityRepository = nationalityRepository;
    }

    @Override
    public List<Nationality> listNationalities() {
        return nationalityRepository.findAll();
    }
}
