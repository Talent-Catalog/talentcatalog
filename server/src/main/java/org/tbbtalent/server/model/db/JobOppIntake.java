/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "job_opp_intake")
@SequenceGenerator(name = "seq_gen", sequenceName = "job_opp_intake_id_seq", allocationSize = 1)
public class JobOppIntake extends AbstractDomainObject<Long> {
    @OneToOne(mappedBy = "jobOppIntake")
    private SalesforceJobOpp jobOpp;
    private String salaryRange;
    private String recruitmentProcess;
    private String employerCostCommitment;
    private String location;
    private String locationDetails;
    private String benefits;
    private String languageRequirements;
    private String educationRequirements;
    private String skillRequirements;
    private String employmentExperience;
    private String occupationCode;
    private String minSalary;
    private String visaPathways;
}
