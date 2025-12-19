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

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.repository.db.EmployerRepository;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.EmployerService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.SalesforceHelper;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployerService {
    private final CountryService countryService;
    private final EmailHelper emailHelper;
    private final EmployerRepository employerRepository;
    private final SalesforceService salesforceService;
    private final UserService userService;

    private Employer createEmployerFromSalesforceAccount(Account account) {
        //Create employer.
        Employer employer = new Employer();
        employer.setName(account.getName());
        employer.setSfId(account.getId());
        employer.setCreatedBy(userService.getLoggedInUser());
        employer.setCreatedDate(OffsetDateTime.now());

        String booleanAsString = account.getHasHiredInternationally();
        Boolean hasHiredInternationally = booleanAsString == null ? null :
            booleanAsString.toLowerCase().startsWith("y");
        employer.setHasHiredInternationally(hasHiredInternationally);
        employer.setDescription((account.getDescription()));
        employer.setWebsite((account.getWebsite()));

        //todo Other account related fields - office size etc

        final String accountCountry = account.getCountry();
        Country country = countryService.findByName(accountCountry);
        employer.setCountry(country);
        if (country == null ){
            emailHelper.sendAlert("Salesforce country " + accountCountry +
                " in SF account " + account.getName() + " not found in database.");
        }

        return employerRepository.save(employer);
    }

    @Override
    public Employer findOrCreateEmployerFromSalesforceAccount(Account account) {
        String sfId = account.getId();
        //Look for it in TC first
        Employer employer = employerRepository.findFirstBySfId(sfId).orElse(null);
        if (employer == null) {
            employer = createEmployerFromSalesforceAccount(account);
        }

        return employer;
    }

    @Override
    @NonNull
    public Employer findOrCreateEmployerFromSalesforceId(@NonNull String sfId) {
        //Look for it in TC first
        Employer employer = employerRepository.findFirstBySfId(sfId).orElse(null);
        if (employer == null) {
            //If not in TC, autocreate - assuming that it exists in Salesforce.
            Account account = salesforceService.findAccount(sfId);
            if (account == null) {
                throw new NoSuchObjectException("No such Salesforce account: " + sfId);
            }
            employer = createEmployerFromSalesforceAccount(account);
        }
        return employer;
    }

    @Override
    @NonNull
    public Employer findOrCreateEmployerFromSalesforceLink(String sflink) {
        String sfId = SalesforceHelper.extractIdFromSfUrl(sflink);
        if (sfId == null) {
            throw new NoSuchObjectException("No such Salesforce url: " + sflink);
        }
        return findOrCreateEmployerFromSalesforceId(sfId);
    }

    @NonNull
    @Override
    public Employer get(long id) throws NoSuchObjectException {
        return employerRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(Employer.class, id));
    }
}
