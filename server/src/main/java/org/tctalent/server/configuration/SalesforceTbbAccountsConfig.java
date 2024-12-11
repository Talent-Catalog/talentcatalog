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
 * MODEL - Loading application.yml NESTED configuration into a Java object
 *
 * @author John Cameron
 */
@Getter
@Setter
@ConfigurationProperties("salesforce.tbb")
public class SalesforceTbbAccountsConfig {

    /**
     * Salesforce account id of TBB Jordan account
     */
    private String jordanAccount;

    /**
     * Salesforce account id of TBB Lebanon account
     */
    private String lebanonAccount;

    /**
     * Salesforce account id of TBB Other account
     */
    private String otherAccount;

    /**
     * Salesforce account id of TBB account
     */
    private String tbbAccount;

}
