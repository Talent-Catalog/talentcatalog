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

package org.tctalent.server.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Salesforce configuration - read from application.yml
 * <p/>
 * MODEL - Loading application.yml configuration into a Java object
 *
 * @author John Cameron
 */
@Getter
@Setter
@ConfigurationProperties("salesforce")
public class SalesforceConfig {

    /**
     * Maximum number of days ago that will be considered recent.
     * Used to examine recently changed open opportunities that might need to be considered.
     * See fetchJobOpportunitiesByIdOrOpenOnSF in SalesforceService
     */
    private int daysAgoRecent;

    /**
     * Private key used for accessing Salesforce
     */
    private String privatekey;

    /**
     * Base URL for API methods that use SF Classic
     */
    private String baseClassicUrl;

    /**
     * Base URL for SF login
     */
    private String baseLoginUrl;

    /**
     *  In the doc for the JWT bearer token, this is referred to as 'client_id' - it is obtained from SF at Setup and differs per SF user > App Manager > View (tbbtalent) > Manage Consumer Details
     */
    private String consumerKey;

    /**
     * The SF user to be associated with the API login
     */
    private String user;

    /**
     * Base url for API methods that use SF Lightning
     */
    private String baseLightningUrl;

}
