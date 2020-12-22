package org.tbbtalent.server.service.db.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.CountrySpecification;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.country.UpdateCountryRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CountryService;
import org.tbbtalent.server.service.db.TranslationService;

import io.jsonwebtoken.lang.Collections;

@Service
public class CountryServiceImpl implements CountryService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(CountryServiceImpl.class);

    @Value("${tbb.destinations}")
    private String[] tbbDestinations;
    private List<Country> tbbDestinationCountries;

    private Map<Long, Country> cache = null;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;
    private final TranslationService translationService;
    private final UserContext userContext;

    @Autowired
    public CountryServiceImpl(CandidateRepository candidateRepository,
                              CountryRepository countryRepository,
                              TranslationService translationService,
                              UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.translationService = translationService;
        this.userContext = userContext;
    }

    @Override
    public void afterPropertiesSet() {
        //Extract the TBB destination countries array from the configuration
        tbbDestinationCountries = new ArrayList<>();
        for (String tbbDestination : tbbDestinations) {
            Country country = countryRepository.findByNameIgnoreCase(tbbDestination);
            if (country == null) {
                log.error("Error in application.yml file. See tbb.destinations. " +
                        "No country found called " + tbbDestination);
            } else {
                tbbDestinationCountries.add(country);
            }
        }
    }

    private void dropCache() {
        cache = null;
    }

    private void loadCache() {
        if (cache == null) {
            cache = new HashMap<>();
            List<Country> countries = countryRepository.findAll();
            for (Country country : countries) {
                cache.put(country.getId(), country);
            }
        }
    }

    @Override
    public List<Country> listCountries() {
        User user = userContext.getLoggedInUser().orElse(null);
        List<Country> countries;

        // Restrict access if there are source countries associated to admin user
        if(user != null && user.getSourceCountries().size() > 0){
            countries = countryRepository.findByStatusAndSourceCountries(Status.active, user.getSourceCountries());
        } else {
            //Note: Can't use cache because translationService modifies it adding 
            //translations - which will always get returned to user (because of
            //the way Dto builder works - if translation is present, it will use 
            //that as name).
            countries = countryRepository.findByStatus(Status.active);
        }
        translationService.translate(countries, "country");
        return countries;
    }

    @Override
    public List<Country> getTBBDestinations() {
        return tbbDestinationCountries;
    }

    @Override
    public Page<Country> searchCountries(SearchCountryRequest request) {
        Page<Country> countries = countryRepository.findAll(
                CountrySpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + countries.getTotalElements() + " countries in search");
        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(countries.getContent(), "country", request.getLanguage());
        }
        return countries;
    }
    
    @Override
    public Country getCountry(long id) {
        loadCache();
        Country country = cache.get(id);
        if (country == null) {
            throw new NoSuchObjectException(Country.class, id);
        }
        return country;
    }

    @Override
    @Transactional
    public Country createCountry(UpdateCountryRequest request) throws EntityExistsException {
        dropCache();
        Country country = new Country(
                request.getName(), request.getStatus());
        checkDuplicates(null, request.getName());
        return this.countryRepository.save(country);
    }

    @Override
    @Transactional
    public Country updateCountry(long id, UpdateCountryRequest request) throws EntityExistsException {
        dropCache();
        Country country = this.countryRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Country.class, id));
        checkDuplicates(id, request.getName());

        country.setName(request.getName());
        country.setStatus(request.getStatus());
        return countryRepository.save(country);
    }

    @Override
    @Transactional
    public boolean deleteCountry(long id) throws EntityReferencedException {
        dropCache();
        Country country = countryRepository.findById(id).orElse(null);
        List<Candidate> candidates = candidateRepository.findByCountryId(id);
        if (!Collections.isEmpty(candidates)){
            throw new EntityReferencedException("country");
        }
        if (country != null) {
            country.setStatus(Status.deleted);
            countryRepository.save(country);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name) {
        Country existing = countryRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("country");
        }
    }
}
