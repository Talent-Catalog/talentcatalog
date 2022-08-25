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

package org.tbbtalent.server.service.db;

import javax.servlet.http.HttpServletRequest;
import org.tbbtalent.server.model.db.RootRequest;

/**
 * Service for managing {@link RootRequest}
 *
 * @author John Cameron
 */
public interface RootRequestService {

    /**
     * Creates a RootRequest based on the given data gathered from an incoming HTTP request.
     * <p/>
     * @param request This is the request that was received. Other parameters have been
     *                automatically extracted from that request by the Spring framework.
     * @param partnerAbbreviation Content of "p=" query
     * @param utmSource Content of "utm_source=" query
     * @param utmMedium Content of "utm_medium=" query
     * @param utmCampaign Content of "utm_campaign=" query
     * @param utmTerm Content of "utm_term=" query
     * @param utmContent Content of "utm_content=" query
     * @return RootRequest object
     */
    RootRequest createRootRequest(HttpServletRequest request, String partnerAbbreviation,
        String utmSource, String utmMedium, String utmCampaign, String utmTerm, String utmContent);
}
