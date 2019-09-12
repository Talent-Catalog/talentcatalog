package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.repository.IndustryRepository;
import org.tbbtalent.server.service.IndustryService;

import java.util.List;

@Service
public class IndustryServiceImpl implements IndustryService {

    private final IndustryRepository industryRepository;

    @Autowired
    public IndustryServiceImpl(IndustryRepository industryRepository) {
        this.industryRepository = industryRepository;
    }

    @Override
    public List<Industry> listIndustries() {
        return industryRepository.findAll();
    }
}
