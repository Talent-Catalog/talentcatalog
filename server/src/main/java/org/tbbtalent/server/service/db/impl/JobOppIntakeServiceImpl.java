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

package org.tbbtalent.server.service.db.impl;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.JobOppIntake;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.repository.db.JobOppIntakeRepository;
import org.tbbtalent.server.repository.db.SalesforceJobOppRepository;
import org.tbbtalent.server.request.job.JobIntakeData;
import org.tbbtalent.server.service.db.JobOppIntakeService;

/**
 * This service handles the data related to the job intake, it is associated with a job opportunity.
 *
 * @author John Cameron
 */
@Service
public class JobOppIntakeServiceImpl implements JobOppIntakeService {
    private final JobOppIntakeRepository jobOppIntakeRepository;
    private final SalesforceJobOppRepository salesforceJobOppRepository;

    public JobOppIntakeServiceImpl(JobOppIntakeRepository jobOppIntakeRepository,
        SalesforceJobOppRepository salesforceJobOppRepository) {
        this.jobOppIntakeRepository = jobOppIntakeRepository;
        this.salesforceJobOppRepository = salesforceJobOppRepository;
    }

    @NonNull
    @Override
    public JobOppIntake get(long id) throws NoSuchObjectException {
        return jobOppIntakeRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(JobOppIntake.class, id));
    }

    @Override
    public void update(long jobOppId, JobIntakeData data) throws NoSuchObjectException {
        SalesforceJobOpp jobOpp = salesforceJobOppRepository.findById(jobOppId)
            .orElseThrow(() -> new NoSuchObjectException(SalesforceJobOpp.class, jobOppId));
        JobOppIntake joi = jobOpp.getJobOppIntake();
        // Create the JOI entry if it doesn't exist, and associate it with the Job Opp.
        if (joi == null) {
            joi = new JobOppIntake();
            jobOpp.setJobOppIntake(joi);
        }
        populateIntakeData(joi, data);
        // Due to the cascade merge type, saving the jobOpp will automatically also save the associated joi object.
        salesforceJobOppRepository.save(jobOpp);
    }

    private void populateIntakeData(JobOppIntake joi, JobIntakeData data) {
        final String salaryRange = data.getSalaryRange();
        if (salaryRange != null) {
            joi.setSalaryRange(salaryRange);
        }
        if (data.getRecruitmentProcess() != null) {
            joi.setRecruitmentProcess(data.getRecruitmentProcess());
        }
        if (data.getEmployerCostCommitment() != null) {
            joi.setEmployerCostCommitment(data.getEmployerCostCommitment());
        }
        if (data.getLocation() != null) {
            joi.setLocation(data.getLocation());
        }
        if (data.getLocationDetails() != null) {
            joi.setLocationDetails(data.getLocationDetails());
        }
        if (data.getBenefits() != null) {
            joi.setBenefits(data.getBenefits());
        }
        if (data.getLanguageRequirements() != null) {
            joi.setLanguageRequirements(data.getLanguageRequirements());
        }
        if (data.getEducationRequirements() != null) {
            joi.setEducationRequirements(data.getEducationRequirements());
        }
        if (data.getSkillRequirements() != null) {
            joi.setSkillRequirements(data.getSkillRequirements());
        }
        if (data.getEmploymentExperience() != null) {
            joi.setEmploymentExperience(data.getEmploymentExperience());
        }
        if (data.getOccupationCode() != null) {
            joi.setOccupationCode(data.getOccupationCode());
        }
        if (data.getMinSalary() != null) {
            joi.setMinSalary(data.getMinSalary());
        }
    }

}
