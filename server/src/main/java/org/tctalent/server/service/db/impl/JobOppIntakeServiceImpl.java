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

package org.tctalent.server.service.db.impl;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.JobOppIntake;
import org.tctalent.server.repository.db.JobOppIntakeRepository;
import org.tctalent.server.request.job.IJobIntakeData;
import org.tctalent.server.service.db.JobOppIntakeService;

/**
 * This service handles the data related to the job intake, it is associated with a job opportunity.
 *
 * @author John Cameron
 */
@Service
public class JobOppIntakeServiceImpl implements JobOppIntakeService {
    private final JobOppIntakeRepository jobOppIntakeRepository;

    public JobOppIntakeServiceImpl(JobOppIntakeRepository jobOppIntakeRepository) {
        this.jobOppIntakeRepository = jobOppIntakeRepository;
    }

    @NonNull
    @Override
    public JobOppIntake get(long id) throws NoSuchObjectException {
        return jobOppIntakeRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(JobOppIntake.class, id));
    }

    @Override
    public JobOppIntake create(IJobIntakeData data) {
        JobOppIntake joi = new JobOppIntake();

        populateIntakeData(joi, data);

        return jobOppIntakeRepository.save(joi);
    }

    @Override
    public void update(long id, IJobIntakeData data) throws NoSuchObjectException {
        JobOppIntake joi = jobOppIntakeRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(JobOppIntake.class, id));

        populateIntakeData(joi, data);

        jobOppIntakeRepository.save(joi);
    }

    private void populateIntakeData(JobOppIntake joi, IJobIntakeData data) {
        if (data.getSalaryRange() != null) {
            joi.setSalaryRange(data.getSalaryRange());
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
        if (data.getVisaPathways() != null) {
            joi.setVisaPathways(data.getVisaPathways());
        }
    }

}
