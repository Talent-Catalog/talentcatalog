package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.service.CountryService;

import java.util.List;

@Service
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    @Autowired
    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public List<Country> listCountries() {
        return countryRepository.findByStatus(Status.active);
    }
}
