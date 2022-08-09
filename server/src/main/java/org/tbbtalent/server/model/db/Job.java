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

package org.tbbtalent.server.model.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//TODO JC Link Job to DB
/**
 * A job represents a Job Opportunity provided by an employer in a destination location.
 * <p/>
 * This data is populated from the corresponding Salesforce Employer Job Opportunity.
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class Job {

    private SalesforceJobOpp sfJobOpp;

    /**
     * This is the official list of candidates which will be submitted to the employer for
     * their consideration.
     * <p/>
     * todo Assert that submissionList should be a registeredJob associated with sfJobOpp
     */
    private SavedList submissionList;

    public String getCountry() {
        return sfJobOpp == null ? null :sfJobOpp.getCountry();
    }

    public String getEmployer() {
        return sfJobOpp == null ? null :sfJobOpp.getEmployer();
    }

    public String getName() {
        return sfJobOpp == null ? null :sfJobOpp.getName();
    }

    public JobOpportunityStage getStage() {
        return sfJobOpp == null ? null :sfJobOpp.getStage();
    }
}
