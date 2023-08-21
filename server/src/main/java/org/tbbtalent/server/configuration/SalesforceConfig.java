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

package org.tbbtalent.server.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

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
     * Root URL directing to SF Classic sandbox or production, depending on environment
     */
    private String baseClassicUrl;

    /**
     * Root URL directing to SF login for sandbox or production, depending on environment
     */
    private String baseLoginUrl;


    private String consumerKey;

    private String user;

    private String baseLightningUrl;

}
