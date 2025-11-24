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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.configuration.properties.DpaProperties;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.PartnerJobRelation;
import org.tctalent.server.model.db.PartnerJobRelationKey;
import org.tctalent.server.model.db.PublicApiPartnerDto;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.PartnerJobRelationRepository;
import org.tctalent.server.repository.db.PartnerRepository;
import org.tctalent.server.repository.db.PartnerSpecification;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.request.partner.UpdatePartnerRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.security.PublicApiKeyGenerator;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.PublicIDService;
import org.tctalent.server.service.db.TermsInfoService;

@Service
@AllArgsConstructor
@Slf4j
public class PartnerServiceImpl implements PartnerService {
    private final CountryService countryService;
    private final AuthService authService;
    private final PartnerRepository partnerRepository;
    private final PartnerJobRelationRepository partnerJobRelationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublicIDService publicIDService;
    private final TermsInfoService termsInfoService;
    private final DpaProperties dpaProperties;

    @Override
    public @NonNull PartnerImpl create(UpdatePartnerRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {

        //Check that name is present - it is a required attribute
        if (request.getName() == null) {
            throw new InvalidRequestException("Missing partner name");
        }

        PartnerImpl partner = new PartnerImpl();
        partner.setPublicId(publicIDService.generatePublicID());

        // Public API access fields
        populatePublicApiAccessFields(request, partner);

        //Populate common attributes
        populateCommonAttributes(request, partner);

        //Source partner attributes
        partner.setRegistrationLandingPage(request.getRegistrationLandingPage());
        partner.setAutoAssignable(request.isAutoAssignable());

        //Source countries
        populateSourceCountries(request.getSourceCountryIds(), partner);

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

    private void populatePublicApiAccessFields(UpdatePartnerRequest request, Partner partner) {
        boolean currentPublicApiAccess = partner.isPublicApiAccess();
        if (currentPublicApiAccess != request.isPublicApiAccess()) {
            //Partner api access has changed:
            if (request.isPublicApiAccess()) {
                //New request for public api access

                // Generate the plain API key
                String plainApiKey = PublicApiKeyGenerator.generateApiKey();
                // Hash the API key for secure storage
                String hashedKey = passwordEncoder.encode(plainApiKey);
                partner.setPublicApiKey(plainApiKey);
                partner.setPublicApiKeyHash(hashedKey);
            } else {
                //Giving up public api access
                //Clear hashed key
                partner.setPublicApiKeyHash(null);
                //Note that disabling a key requires clearing the cache (through restart or otherwise)
            }
        }
        //Update public api authorities (even if not currently using public api)
        partner.setPublicApiAuthorities(request.getPublicApiAuthorities());
    }

    private void populateSourceCountries(Set<Long> sourceCountryIds, Partner partner) {
        Set<Country> sourceCountries = new HashSet<>();
        if (sourceCountryIds != null && !sourceCountryIds.isEmpty()) {
            //Check that all countries are known - populate set
            for (Long sourceCountryId : sourceCountryIds) {
                Country country = countryService.getCountry(sourceCountryId);
                sourceCountries.add(country);
            }
        }
        partner.setSourceCountries(sourceCountries);
    }

    @Nullable
    @Override
    public PublicApiPartnerDto findPublicApiPartnerDtoByKey(String apiKey) {
        //Iterate though all users with public api key hashes - finding one that matches the apiKey.
        //Note that you can't just compute the apiKey hash and look up for a matching hash because
        //each hash call on the same apiKey will produce a different hash value - this is because
        //a random "salt" is added to each hash call.
        //See, for example, https://auth0.com/blog/hashing-in-action-understanding-bcrypt/
        //
        //Note also that the public API server stores successfully validated apiKeys and their
        //associated partners in an in memory hash table (cache) so this method is only called for
        //the first time each ApiKey is encountered - or when the hashtable is cleared (eg server
        //restarts).

        PublicApiPartnerDto partner = partnerRepository.findPublicApiPartnerDtos().stream()
            .filter(p -> passwordEncoder.matches(apiKey, p.getPublicApiKeyHash()))
            .findFirst()
            .orElse(null);

        return partner;
    }

    @NonNull
    @Override
    public Partner findByPublicId(String publicId) {
        final PartnerImpl partner = partnerRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NoSuchObjectException(Partner.class, publicId));

        return partner;
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
    public List<PartnerImpl> listActiveSourcePartners() {
        SearchPartnerRequest request = new SearchPartnerRequest();
        request.setSourcePartner(true);
        request.setStatus(Status.active);
        return search(request);
    }

    @Override
    public List<PartnerImpl> listAllSourcePartners() {
        SearchPartnerRequest request = new SearchPartnerRequest();
        request.setSourcePartner(true);
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
    public void setPublicIds(List<PartnerImpl> partners) {
        for (Partner partner : partners) {
            if (partner.getPublicId() == null) {
                partner.setPublicId(publicIDService.generatePublicID());
            }
        }
        if (!partners.isEmpty()) {
            partnerRepository.saveAll(partners);
        }
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

        // Public API access fields
        populatePublicApiAccessFields(request, partner);

        //Source countries
        populateSourceCountries(request.getSourceCountryIds(), partner);

        //Populate common attributes
        populateCommonAttributes(request, partner);

        //Source partner attributes
        partner.setNotificationEmail(request.getNotificationEmail());
        partner.setDefaultPartnerRef(request.isDefaultPartnerRef());
        partner.setRegistrationLandingPage(request.getRegistrationLandingPage());
        partner.setAutoAssignable(request.isAutoAssignable());

        if (request.getRedirectPartnerId() != null) {
            PartnerImpl newPartner = manageRedirectPartnerAssignment(request.getRedirectPartnerId());
            partner.setRedirectPartner(newPartner);

            LogBuilder.builder(log)
                .action("redirectInactivePartnerUrl")
                .message("URLs identifying inactive partner " + partner.getName() +
                    " will now redirect to " + newPartner.getName() + ".")
                .logInfo();
        } else {
          partner.setRedirectPartner(null);
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

    private PartnerImpl manageRedirectPartnerAssignment(long redirectPartnerId) {
        // Get the new partner that URLs will redirect to
        PartnerImpl newPartner = (PartnerImpl) getPartner(redirectPartnerId);

        // The partner we're redirecting to could itself have been deactivated in the past and
        // therefore have had a redirectPartner assigned to it that we now wish to ignore -
        // not only for operational reasons but also to avoid getting stuck in a recursive loop!
        if (newPartner.getRedirectPartner() != null) {
            newPartner.setRedirectPartner(null);
            newPartner = partnerRepository.save(newPartner);
        }

        return newPartner;
    }

    @Override
    public PartnerImpl updateAcceptedDpa(String acceptedDpaId) {
        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        PartnerImpl partner = user.getPartner();
        updatePolicyId(acceptedDpaId, partner);
        partner = partnerRepository.save(partner);
        return partner;
    }

    /**
     * Factored out some common code
     */
    private static void updatePolicyId(String acceptedDpaId, PartnerImpl partner) {
        if (acceptedDpaId == null) {
            throw new InvalidRequestException("Privacy policy has not been accepted");
        }
        partner.setAcceptedDataProcessingAgreementId(acceptedDpaId);
        partner.setAcceptedDataProcessingAgreementDate(OffsetDateTime.now());
    }

    @Transactional
    public PartnerImpl setFirstDpaSeen() {
        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        PartnerImpl partner = user.getPartner();
        if (partner.isSourcePartner() && partner.getFirstDpaSeenDate() == null) {
            partner.setFirstDpaSeenDate(OffsetDateTime.now());
            partner = partnerRepository.save(partner);
        }
        return partner;
    }
    @Transactional
    public boolean requiresDpaAcceptance() {
        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        PartnerImpl partner = user.getPartner();

        if (!partner.isSourcePartner()) {
            return false; // Non-partner users don't require DPA
        }

        // Get current DPA
        TermsInfo currentDpa = termsInfoService.getCurrentByType(TermsType.DATA_PROCESSING_AGREEMENT);
        // Partner already accepted the current DPA
        if (partner.getAcceptedDataProcessingAgreementId() != null &&
            partner.getAcceptedDataProcessingAgreementId().equals(currentDpa.getId())) {
            return false;
        }
        // No first seen date or within 90 days
        return partner.getFirstDpaSeenDate() != null &&
          !OffsetDateTime.now().isBefore(partner.getFirstDpaSeenDate().plusDays(dpaProperties.getDpaVisibilityDelayDays()));
    }
}
