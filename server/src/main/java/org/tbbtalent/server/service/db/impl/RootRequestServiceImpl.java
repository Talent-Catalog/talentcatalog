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

import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.RootRequest;
import org.tbbtalent.server.repository.db.RootRequestRepository;
import org.tbbtalent.server.service.db.RootRequestService;

@Service
public class RootRequestServiceImpl implements RootRequestService {

    private final RootRequestRepository rootRequestRepository;

    public RootRequestServiceImpl(RootRequestRepository rootRequestRepository) {
        this.rootRequestRepository = rootRequestRepository;
    }

    @Override
    public RootRequest createRootRequest(HttpServletRequest request, String partnerAbbreviation,
        String utmSource, String utmMedium, String utmCampaign, String utmTerm, String utmContent) {
        RootRequest rootRequest = new RootRequest();

        String ipAddress = request.getHeader("X-Forward-For");
        if(ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        rootRequest.setIpAddress(ipAddress);
        rootRequest.setTimestamp(Instant.now());

        rootRequest.setRequestUrl(request.getRequestURL().toString());
        rootRequest.setQueryString(request.getQueryString());
        rootRequest.setPartnerAbbreviation(partnerAbbreviation);
        rootRequest.setUtmSource(utmSource);
        rootRequest.setUtmMedium(utmMedium);
        rootRequest.setUtmCampaign(utmCampaign);
        rootRequest.setUtmTerm(utmTerm);
        rootRequest.setUtmContent(utmContent);

        return rootRequestRepository.save(rootRequest);
    }
}
