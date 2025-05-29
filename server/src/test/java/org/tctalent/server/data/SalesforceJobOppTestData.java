/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.data;

import java.time.LocalDate;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;

public class SalesforceJobOppTestData {

    public static SalesforceJobOpp getJob() {
        SalesforceJobOpp job = new SalesforceJobOpp();
        job.setSfId("LKJH66446GGFDFSA");
        job.setAccountId("JKDHUT0000JJJGGG");
        job.setName("Test Job");
        job.setNextStep("Do something");
        job.setNextStepDueDate(LocalDate.of(1901, 1, 1));
        job.setStage(JobOpportunityStage.candidateSearch);
        job.setEmployerEntity(getEmployer());
        return job;
    }

    static Employer getEmployer() {
        Employer employer = new Employer();
        employer.setName("Test Employer");
        employer.setSfId("LKJH66446GGFDFSA");
        return employer;
    }

}
