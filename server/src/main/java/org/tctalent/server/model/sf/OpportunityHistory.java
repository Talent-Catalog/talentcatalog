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
 * Represents a Salesforce OpportunityHistory record.
 * <p/>
 * There are multiple instances of these associated with each Salesforce Opportunity, one for each
 * time the opportunity stage changes.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class OpportunityHistory extends SalesforceObjectBase {

    @JsonSetter("OpportunityId")
    private String opportunityId;

    @JsonSetter("StageName")
    private String stageName;

    @JsonSetter("SystemModstamp")
    private String systemModstamp;

    @Override
    String getSfObjectName() {
        return "OpportunityHistory";
    }

}
