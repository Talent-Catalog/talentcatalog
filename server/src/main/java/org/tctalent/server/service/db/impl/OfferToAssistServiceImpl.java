/*
 * Copyright (c) 2025 Talent Catalog.
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
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCouponCode;
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.OfferToAssistRepository;
import org.tctalent.server.repository.db.OfferToAssistSpecification;
import org.tctalent.server.request.KeywordPagedSearchRequest;
import org.tctalent.server.request.OfferToAssistRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.OfferToAssistService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.PublicIDService;
import org.tctalent.server.service.db.UserService;

@Service
@AllArgsConstructor
@Slf4j
public class OfferToAssistServiceImpl implements OfferToAssistService {
    private final CandidateService candidateService;
    private final OfferToAssistRepository offerToAssistRepository;
    private final PartnerService partnerService;
    private final PublicIDService publicIDService;
    private final UserService userService;

    @Override
    public @NonNull OfferToAssist createOfferToAssist(OfferToAssistRequest request)
        throws NoSuchObjectException {

        //Convert candidates referred to in the request's CandidateCoupon list from publicIds
        //to actual candidate references.
        List<CandidateCouponCode> candidateCouponCodes = new ArrayList<>();
        for (org.tctalent.anonymization.model.CandidateCoupon candidateCouponRequest :
            request.getCandidates()) {

            final String publicId = candidateCouponRequest.getPublicId();
            Candidate candidate = candidateService.findByPublicId(publicId);
            if (candidate == null) {
                throw new NoSuchObjectException(Candidate.class, publicId);
            } else {
                CandidateCouponCode candidateCouponCode = new CandidateCouponCode();
                candidateCouponCode.setCandidate(candidate);
                candidateCouponCode.setCouponCode(candidateCouponRequest.getCouponCode());
                candidateCouponCodes.add(candidateCouponCode);
            }
        }

        //Look up partner - throws Exception if not found
        final Long partnerId = request.getPartnerId();
        Partner partner = partnerService.getPartner(partnerId);

        OfferToAssist ota = new OfferToAssist();
        ota.setPublicId(publicIDService.generatePublicID());

        ota.setCandidateCouponCodes(candidateCouponCodes);

        ota.setAdditionalNotes(request.getAdditionalNotes());
        ota.setPartner((PartnerImpl) partner);
        ota.setReason(request.getReason());

        ota.setCreatedBy(userService.getLoggedInUser());
        ota.setCreatedDate(OffsetDateTime.now());

        ota = offerToAssistRepository.save(ota);

        //Now update candidate coupons with reference to the OTA
        for (CandidateCouponCode candidateCouponCode : candidateCouponCodes) {
            candidateCouponCode.setOfferToAssist(ota);
        }

        //Assign coupons to ota
        ota.setCandidateCouponCodes(candidateCouponCodes);

        //Update db and return
        return offerToAssistRepository.save(ota);
    }

    @Override
    public Page<OfferToAssist> searchOffersToAssist(KeywordPagedSearchRequest request) {
        Page<OfferToAssist> otas = offerToAssistRepository.findAll(
                OfferToAssistSpecification.buildSearchQuery(request), request.getPageRequest());

        LogBuilder.builder(log)
                .action("SearchOffersToAssist")
                .message("Found " + otas.getTotalElements() + " offers to assist in search")
                .logInfo();

        return otas;
    }
}
