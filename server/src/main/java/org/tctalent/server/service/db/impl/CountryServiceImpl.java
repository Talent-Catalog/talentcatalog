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

import io.jsonwebtoken.lang.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.CountrySpecification;
import org.tctalent.server.request.country.SearchCountryRequest;
import org.tctalent.server.request.country.UpdateCountryRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.TranslationService;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.locale.LocaleHelper;

@Service
@Slf4j
public class CountryServiceImpl implements CountryService, InitializingBean {

    @Value("${tbb.destinations}")
    private String[] tcDestinations;
    private List<Country> tcDestinationCountries;

    private Map<Long, Country> cache = null;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;
    private final TranslationService translationService;
    private final AuthService authService;

    @Autowired
    public CountryServiceImpl(CandidateRepository candidateRepository,
                              CountryRepository countryRepository,
                              TranslationService translationService,
                              AuthService authService) {
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.translationService = translationService;
        this.authService = authService;
    }

    @Override
    public void afterPropertiesSet() {
        //Extract the TC destination countries array from the configuration
        tcDestinationCountries = new ArrayList<>();
        for (String tcDestination : tcDestinations) {
            Country country = countryRepository.findByNameIgnoreCase(tcDestination);
            if (country == null) {
                LogBuilder.builder(log)
                    .action("CountryServiceImpl")
                    .message("Error in application.yml file. See tbb.destinations. " +
                            "No country found called " + tcDestination)
                    .logError();
            } else {
                tcDestinationCountries.add(country);
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
    public List<Country> listCountries(Boolean restricted) {
        User user = authService.getLoggedInUser().orElse(null);
        List<Country> countries;

        if (restricted) {
            // Restrict access if there are source countries associated to admin user
            if(user != null && user.getSourceCountries().size() > 0 ){
                countries = countryRepository.findByStatusAndSourceCountries(Status.active, user.getSourceCountries());
            } else {
                //Note: Can't use cache because translationService modifies it adding
                //translations - which will always get returned to user (because of
                //the way Dto builder works - if translation is present, it will use
                //that as name).
                countries = countryRepository.findByStatus(Status.active);
            }
        } else {
            countries = countryRepository.findByStatus(Status.active);
        }
        translationService.translate(countries, "country");
        return countries;
    }

    @Override
    public List<Country> getTCDestinations() {
        return tcDestinationCountries;
    }

    @Override
    public Page<Country> searchCountries(SearchCountryRequest request) {
        Page<Country> countries = countryRepository.findAll(
                CountrySpecification.buildSearchQuery(request), request.getPageRequest());

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("searchCountries")
            .message("Found " + countries.getTotalElements() + " countries in search")
            .logInfo();

        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(countries.getContent(), "country", request.getLanguage());
        }
        return countries;
    }

    @Override
    @NonNull
    public Country getCountry(long id) throws NoSuchObjectException {
        loadCache();
        Country country = cache.get(id);
        if (country == null) {
            throw new NoSuchObjectException(Country.class, id);
        }
        return country;
    }

    @NonNull
    @Override
    public Country findByIsoCode(String isoCode) {
        return countryRepository.findByIsoCode(isoCode)
            .orElseThrow(() ->new NoSuchObjectException(Country.class, isoCode));
    }

    @Override
    public Country findByName(String name) {
        return countryRepository.findByNameIgnoreCase(name);
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

    @Override
    public DtoBuilder selectBuilder() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("isoCode")
            .add("status")
            ;
    }

    @Override
    public String updateIsoCodes() {
        StringBuilder sb = new StringBuilder();

        List<Country> countries = listCountries(false);

        Map<String, String> xlLang = LocaleHelper.getCountryNameTranslations("en");
        //Create reverse map - English name to code.

        Map<String, String> nameToCode = new HashMap<>();
        for (Entry<String, String> codeNameEntry : xlLang.entrySet()) {
            nameToCode.put(codeNameEntry.getValue(), codeNameEntry.getKey());
        }

        //Now go through countries, using name to look up code
        for (Country country : countries) {
            final String name = country.getName().trim();
            String code = nameToCode.get(name);
            if (code == null) {
                if (sb.length() > 0) {
                   sb.append(",");
                }
                sb.append(name);
            } else {
                //Update iso code of country.
                country.setIsoCode(code);
                countryRepository.save(country);
            }
        }

        return sb.toString();
    }

    private void checkDuplicates(Long id, String name) {
        Country existing = countryRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id)){
            throw new EntityExistsException("country");
        }
    }
}
