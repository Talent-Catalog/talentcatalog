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
 * Salesforce RecordType configuration - read from application.yml
 * <p/>
 * MODEL - Loading application.yml NESTED configuration into a Java object
 * MODEL Camel case configuration property (recordType) must be converted to kebab form (record-type)
 *
 * @author John Cameron
 */
@Getter
@Setter
@ConfigurationProperties("salesforce.record-type")
public class SalesforceRecordTypeConfig {

    /**
     * Salesforce RecordType ID of Employer Job opportunities
     */
    private String employerJob;
}
