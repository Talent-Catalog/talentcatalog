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

package org.tctalent.server.model.sf;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Salesforce Account.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Account extends SalesforceObjectBase {

    @JsonSetter("Description")
    private String description;

    @JsonSetter("Website")
    private String website;

    @JsonSetter("Name")
    private String name;

    @JsonSetter("BillingCountry")
    private String country;

    @JsonSetter("Has_Hired_Internationally__c")
    private String hasHiredInternationally;

    @Override
    String getSfObjectName() {
        return "Account";
    }

}
