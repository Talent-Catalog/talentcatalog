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
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.PartnerJobRelation;
import org.tbbtalent.server.model.db.PartnerJobRelationKey;
import org.tbbtalent.server.model.db.RecruiterPartnerImpl;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SourcePartnerImpl;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.model.db.partner.RecruiterPartner;
import org.tbbtalent.server.model.db.partner.SourcePartner;
import org.tbbtalent.server.repository.db.PartnerJobRelationRepository;
import org.tbbtalent.server.repository.db.PartnerRepository;
import org.tbbtalent.server.repository.db.PartnerSpecification;
import org.tbbtalent.server.request.partner.SearchPartnerRequest;
import org.tbbtalent.server.request.partner.UpdatePartnerRequest;
import org.tbbtalent.server.service.db.CountryService;
import org.tbbtalent.server.service.db.PartnerService;

@Service
public class PartnerServiceImpl implements PartnerService {
    private final PartnerRepository partnerRepository;
    private final PartnerJobRelationRepository partnerJobRelationRepository;
    private final CountryService countryService;
    private static final Logger log = LoggerFactory.getLogger(PartnerServiceImpl.class);

    public PartnerServiceImpl(
        PartnerRepository partnerRepository,
        CountryService countryService,
        PartnerJobRelationRepository partnerJobRelationRepository) {
        this.partnerRepository = partnerRepository;
        this.countryService = countryService;
        this.partnerJobRelationRepository = partnerJobRelationRepository;
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
                sourcePartner.setAutoAssignable(request.isAutoAssignable());
                sourcePartner.setSourceCountries(sourceCountries);

                partner = sourcePartner;
                break;

            case "RecruiterPartner":
                RecruiterPartnerImpl recruiterPartner = new RecruiterPartnerImpl();

                populateCommonAttributes(request, recruiterPartner);

                partner = recruiterPartner;
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
    public SourcePartner getDefaultSourcePartner() throws NoSuchObjectException {
        final SourcePartnerImpl partner = (SourcePartnerImpl) partnerRepository.findByDefaultSourcePartner(true)
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
                log.warn("Could not find partner matching abbreviation: " + partnerAbbreviation);
            }
        }
        return partner;
    }

    @Override
    public List<PartnerImpl> listSourcePartners() {
        SearchPartnerRequest request = new SearchPartnerRequest();
        request.setPartnerType("SourcePartner");
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

        Partner partner = getPartner(id);

        if (partner instanceof SourcePartner) {
            SourcePartner sourcePartner = (SourcePartner) partner;

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
            sourcePartner.setNotificationEmail(request.getNotificationEmail());
            sourcePartner.setDefaultPartnerRef(request.isDefaultPartnerRef());
            sourcePartner.setRegistrationLandingPage(request.getRegistrationLandingPage());
            sourcePartner.setAutoAssignable(request.isAutoAssignable());
            sourcePartner.setSourceCountries(sourceCountries);
        } else if (partner instanceof RecruiterPartner) {
            populateCommonAttributes(request, partner);
        }

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
