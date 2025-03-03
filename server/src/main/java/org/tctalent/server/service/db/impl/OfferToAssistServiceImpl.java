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

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.anonymization.model.OfferToAssistCandidatesRequest;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCoupon;
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.repository.db.OfferToAssistRepository;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.OfferToAssistService;
import org.tctalent.server.service.db.PublicIDService;

@Service
@AllArgsConstructor
@Slf4j
public class OfferToAssistServiceImpl implements OfferToAssistService {
    private final CandidateService candidateService;
    private final OfferToAssistRepository offerToAssistRepository;
    private final PublicIDService publicIDService;

    @Override
    public @NonNull OfferToAssist createOfferToAssist(OfferToAssistCandidatesRequest request)
        throws NoSuchObjectException {

        //Convert candidates referred to in the request's CandidateCoupon list from publicIds
        //to actual candidate references.
        List<CandidateCoupon> candidateCoupons = new ArrayList<>();
        for (org.tctalent.anonymization.model.CandidateCoupon candidateCouponRequest :
            request.getCandidates()) {

            final String publicId = candidateCouponRequest.getPublicId();
            Candidate candidate = candidateService.findByPublicId(publicId);
            if (candidate == null) {
                throw new NoSuchObjectException(Candidate.class, publicId);
            } else {
                String coupon = candidateCouponRequest.getCouponCode();
                CandidateCoupon candidateCoupon = new CandidateCoupon(candidate, coupon);
                candidateCoupons.add(candidateCoupon);
            }
        }

        OfferToAssist ota = new OfferToAssist();
        ota.setPublicId(publicIDService.generatePublicID());

        ota.setCandidateCoupons(candidateCoupons);

        ota.setAdditionalNotes(request.getAdditionalNotes());
        ota.setReason(request.getReason());

        return offerToAssistRepository.save(ota);
    }
}
