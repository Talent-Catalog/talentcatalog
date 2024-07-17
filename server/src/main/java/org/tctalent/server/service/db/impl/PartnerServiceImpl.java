/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.PartnerJobRelation;
import org.tctalent.server.model.db.PartnerJobRelationKey;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.PartnerJobRelationRepository;
import org.tctalent.server.repository.db.PartnerRepository;
import org.tctalent.server.repository.db.PartnerSpecification;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.request.partner.UpdatePartnerRequest;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.PartnerService;

@Service
@AllArgsConstructor
@Slf4j
public class PartnerServiceImpl implements PartnerService {
    private final PartnerRepository partnerRepository;
    private final PartnerJobRelationRepository partnerJobRelationRepository;
    private final CountryService countryService;

    @Override
    public @NonNull PartnerImpl create(UpdatePartnerRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {

        //Check that name is present - it is a required attribute
        if (request.getName() == null) {
            throw new InvalidRequestException("Missing partner name");
        }

        PartnerImpl partner = new PartnerImpl();

        //Populate common attributes
        populateCommonAttributes(request, partner);

        //Source partner attributes
        partner.setRegistrationLandingPage(request.getRegistrationLandingPage());
        partner.setAutoAssignable(request.isAutoAssignable());

        //Source countries
        Set<Country> sourceCountries = new HashSet<>();
        Set<Long> sourceCountryIds = request.getSourceCountryIds();
        if (sourceCountryIds != null && !sourceCountryIds.isEmpty()) {
            //Check that all countries are known - populate set
            for (Long sourceCountryId : sourceCountryIds) {
                Country country = countryService.getCountry(sourceCountryId);
                sourceCountries.add(country);
            }
        }
        partner.setSourceCountries(sourceCountries);

        return partnerRepository.save(partner);
    }

    private void populateCommonAttributes(UpdatePartnerRequest request, Partner partner) {

        partner.setAbbreviation(request.getAbbreviation());
        partner.setDefaultContact(request.getDefaultContact());
        partner.setEmployer(request.getEmployer());
        partner.setJobCreator(request.isJobCreator());
        partner.setLogo(request.getLogo());
        partner.setName(request.getName());
        partner.setSourcePartner(request.isSourcePartner());

        Status status = request.getStatus();
        partner.setStatus(status == null ? Status.active : status);

        partner.setSflink(request.getSflink());
        partner.setWebsiteUrl(request.getWebsiteUrl());
    }

    @NonNull
    @Override
    public Partner getPartner(long partnerId) throws NoSuchObjectException {
        final PartnerImpl partner = partnerRepository.findById(partnerId)
            .orElseThrow(() -> new NoSuchObjectException(Partner.class, partnerId));

        return partner;
    }

    @Nullable
    @Override
    public Partner getAutoAssignablePartnerByCountry(@Nullable Country country) {
        Partner partner = null;
        if (country != null) {
            List<PartnerImpl> partners = partnerRepository.findSourcePartnerByAutoassignableCountry(country);
            //Don't select if there is more than one country
            if (partners.size() == 1) {
                partner = partners.get(0);
            }
        }
        return partner;
    }

    @NonNull
    @Override
    public Partner getDefaultSourcePartner() throws NoSuchObjectException {
        final PartnerImpl partner = partnerRepository.findByDefaultSourcePartner(true)
            .orElseThrow(() -> new NoSuchObjectException(Partner.class, "default"));

        return partner;
    }

    @Nullable
    @Override
    public Partner getPartnerFromAbbreviation(@Nullable String partnerAbbreviation) {
        Partner partner = null;
        if (partnerAbbreviation != null) {
            partner = partnerRepository.findByAbbreviation(partnerAbbreviation).orElse(null);
            if (partner == null) {
                //Log a warning.
                LogBuilder.builder(log)
                    .action("GetPartnerFromAbbreviation")
                    .message("Could not find partner matching abbreviation: " + partnerAbbreviation)
                    .logWarn();
            }
        }
        return partner;
    }

    @Override
    public List<PartnerImpl> listPartners() {
        List<PartnerImpl> partners = partnerRepository.findByStatusOrderByName(Status.active);
        return partners;
    }

    @Override
    public List<PartnerImpl> listSourcePartners() {
        SearchPartnerRequest request = new SearchPartnerRequest();
        request.setSourcePartner(true);
        request.setStatus(Status.active);
        return search(request);
    }

    @Override
    public List<PartnerImpl> search(SearchPartnerRequest request) {
        List<PartnerImpl> partners = partnerRepository.findAll(
            PartnerSpecification.buildSearchQuery(request), request.getSort());
        return partners;
    }

    @Override
    public Page<PartnerImpl> searchPaged(SearchPartnerRequest request) {
        Page<PartnerImpl> partners = partnerRepository.findAll(
            PartnerSpecification.buildSearchQuery(request), request.getPageRequest());
        return partners;
    }

    @Override
    public @NonNull PartnerImpl update(long id, UpdatePartnerRequest request)
        throws InvalidRequestException, NoSuchObjectException {

        //Check that defaultContact has been populated from defaultContactId in the request
        if (request.getDefaultContactId() != null && request.getDefaultContact() == null) {
            throw new InvalidRequestException(
                "Bug: UpdatePartnerRequest has not been preprocessed to populate defaultContact");
        }

        Partner partner = getPartner(id);

        Set<Country> sourceCountries = new HashSet<>();
        Set<Long> sourceCountryIds = request.getSourceCountryIds();
        if (sourceCountryIds != null) {
            //Check that all countries are known - populate set
            for (Long sourceCountryId : sourceCountryIds) {
                Country country = countryService.getCountry(sourceCountryId);
                sourceCountries.add(country);
            }
        }

        //Populate common attributes
        populateCommonAttributes(request, partner);

        //Source partner attributes
        partner.setNotificationEmail(request.getNotificationEmail());
        partner.setDefaultPartnerRef(request.isDefaultPartnerRef());
        partner.setRegistrationLandingPage(request.getRegistrationLandingPage());
        partner.setAutoAssignable(request.isAutoAssignable());
        partner.setSourceCountries(sourceCountries);

        return partnerRepository.save((PartnerImpl) partner);
    }

    @Override
    public void updateJobContact(Partner partner, SalesforceJobOpp job, User contactUser) {
        PartnerJobRelationKey key =
            new PartnerJobRelationKey(partner.getId(), job.getId());
        PartnerJobRelation pjr = partnerJobRelationRepository.findById(key).orElse(null);
        if (pjr == null) {
            PartnerImpl partnerImpl = (PartnerImpl) partner;
            pjr = new PartnerJobRelation(partnerImpl, job);
            partnerImpl.getPartnerJobRelations().add(pjr);
        }
        pjr.setContact(contactUser);
        partnerJobRelationRepository.save(pjr);
    }
}
