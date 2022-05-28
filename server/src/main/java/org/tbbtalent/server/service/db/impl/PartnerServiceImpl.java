/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import java.util.HashSet;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.SourcePartnerImpl;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.model.db.partner.SourcePartner;
import org.tbbtalent.server.repository.db.PartnerRepository;
import org.tbbtalent.server.repository.db.PartnerSpecification;
import org.tbbtalent.server.request.partner.SearchPartnerRequest;
import org.tbbtalent.server.request.partner.UpdatePartnerRequest;
import org.tbbtalent.server.service.db.CountryService;
import org.tbbtalent.server.service.db.PartnerService;

@Service
public class PartnerServiceImpl implements PartnerService {
    private final PartnerRepository partnerRepository;
    private final CountryService countryService;

    public PartnerServiceImpl(
        PartnerRepository partnerRepository,
        CountryService countryService) {
        this.partnerRepository = partnerRepository;
        this.countryService = countryService;
    }

    @Override
    public @NonNull PartnerImpl create(UpdatePartnerRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {

        //Check that name is present - it is a required attribute
        if (request.getName() == null) {
            throw new InvalidRequestException("Missing partner name");
        }

        PartnerImpl partner;
        final String partnerType = request.getPartnerType();
        switch (partnerType) {
            case "SourcePartner":
                SourcePartnerImpl sourcePartner = new SourcePartnerImpl();

                //Check that registrationUrl is unique
                String registrationUrl = request.getRegistrationUrl();
                if (registrationUrl != null) {
                    if (getPartnerFromHost(registrationUrl) != null) {
                        throw new EntityExistsException("registration domain");
                    }
                }

                Set<Country> sourceCountries = new HashSet<>();
                Set<Long> sourceCountryIds = request.getSourceCountryIds();
                if (sourceCountryIds != null && !sourceCountryIds.isEmpty()) {
                    //Check that all countries are known - populate set
                    for (Long sourceCountryId : sourceCountryIds) {
                        Country country = countryService.getCountry(sourceCountryId);
                        sourceCountries.add(country);
                    }
                }

                //Populate common attributes
                populateCommonAttributes(request, sourcePartner);

                //Source partner attributes
                sourcePartner.setRegistrationLandingPage(request.getRegistrationLandingPage());
                sourcePartner.setRegistrationUrl(registrationUrl);
                sourcePartner.setSourceCountries(sourceCountries);

                partner = sourcePartner;
                break;

            default:
                throw new InvalidRequestException("Unknown partner type: " + partnerType);
        }

        return partnerRepository.save(partner);
    }

    private void populateCommonAttributes(UpdatePartnerRequest request, Partner partner) {
        partner.setAbbreviation(request.getAbbreviation());
        partner.setLogo(request.getLogo());
        partner.setName(request.getName());

        Status status = request.getStatus();
        partner.setStatus(status == null ? Status.active : status);

        partner.setWebsiteUrl(request.getWebsiteUrl());
    }

    @NonNull
    @Override
    public Partner get(long partnerId) throws NoSuchObjectException {
        final PartnerImpl partner = partnerRepository.findById(partnerId)
            .orElseThrow(() -> new NoSuchObjectException(Partner.class, partnerId));

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
    public Partner getPartnerFromHost(String hostDomain) {
        final PartnerImpl partner = partnerRepository.findByRegistrationUrl(hostDomain)
            .orElse(null);

        return partner;
    }

    @Override
    public Page<PartnerImpl> searchPartners(SearchPartnerRequest request) {
        Page<PartnerImpl> partners = partnerRepository.findAll(
            PartnerSpecification.buildSearchQuery(request), request.getPageRequest());
        return partners;
    }

    @Override
    public @NonNull PartnerImpl update(long id, UpdatePartnerRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {

        Partner partner = get(id);

        if (partner instanceof SourcePartner) {
            SourcePartner sourcePartner = (SourcePartner) partner;

            //Check that any changed registrationUrl is unique
            String registrationUrl = request.getRegistrationUrl();
            if (registrationUrl != null
                && !registrationUrl.equals(sourcePartner.getRegistrationUrl())) {
                if (getPartnerFromHost(registrationUrl) != null) {
                    throw new EntityExistsException("registration domain");
                }
            }

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
            populateCommonAttributes(request, sourcePartner);

            //Source partner attributes
            sourcePartner.setRegistrationLandingPage(request.getRegistrationLandingPage());
            sourcePartner.setRegistrationUrl(registrationUrl);
            sourcePartner.setSourceCountries(sourceCountries);
        }

        return partnerRepository.save((PartnerImpl) partner);
    }
}
