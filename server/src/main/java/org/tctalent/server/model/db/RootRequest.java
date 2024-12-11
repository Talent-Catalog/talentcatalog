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

package org.tctalent.server.model.db;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Records internet requests which have no path but do contain a query or are a subdomain - ie
 * hits on our "root" domain name.
 * <p/>
 * These simple hits on our tctalent.org server will normally be referrals from Google searches,
 * or links from posts we have made on Facebook, Twitter or blogs - or maybe links we have sent
 * out in emails.
 * <p/>
 * The purpose of the RootRequest is to record any query parameters that were appended to these
 * links for the purposes of tracking their origins.
 * In particular, parameters identifying the partner associated with the referral,
 * as well as standard UTM parameters.
 * See https://en.wikipedia.org/wiki/UTM_parameters
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "root_request")
@SequenceGenerator(name = "seq_gen", sequenceName = "root_request_id_seq", allocationSize = 1)
public class RootRequest extends AbstractDomainObject<Long> implements HasTcQueryParameters {

    /**
     * IP address of request.
     */
    private String ipAddress;

    /**
     * Partner parameter as specified by "p=" query parameter
     */
    private String partnerAbbreviation;

    /**
     * Referrer parameter as specified by "r=" query parameter
     */
    private String referrerParam;

    /**
     * Whole query string as returned by {@link HttpServletRequest#getQueryString()}
     */
    private String queryString;

    /**
     * Whole url as returned by {@link HttpServletRequest#getRequestURL()}
     */
    private String requestUrl;

    /**
     * Time at which request was processed
     */
    private Instant timestamp;

    /*
       See, for example, here for description of UTM parameters...
       https://en.wikipedia.org/wiki/UTM_parameters
     */

    /**
     * Campaign as specified by "utm_campaign" query parameter
     */
    private String utmCampaign;

    /**
     * Content as specified by "utm_content" query parameter
     */
    private String utmContent;

    /**
     * Medium as specified by "utm_medium" query parameter
     */
    private String utmMedium;

    /**
     * Source as specified by "utm_source" query parameter
     */
    private String utmSource;

    /**
     * Term as specified by "utm_term" query parameter
     */
    private String utmTerm;
}
