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

package org.tbbtalent.server.api.admin;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.JobOppIntake;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.request.job.JobIntakeData;
import org.tbbtalent.server.service.db.JobOppIntakeService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/joi")
public class JobOppIntakeAdminApi {
    private final JobOppIntakeService jobOppIntakeService;
    private final SalesforceJobOppService salesforceJobOppService;

    public JobOppIntakeAdminApi(JobOppIntakeService jobOppIntakeService,
        SalesforceJobOppService salesforceJobOppService) {
        this.jobOppIntakeService = jobOppIntakeService;
        this.salesforceJobOppService = salesforceJobOppService;
    }
    
    @GetMapping("{id}")
    public @NotNull Map<String, Object> get(@PathVariable("id") long jobOppId) throws NoSuchObjectException {
        JobOppIntake joi = jobOppIntakeService.get(jobOppId);
        return joiDto().build(joi);
    }
    
    @PutMapping("{id}/intake")
    public void update(@PathVariable("id") long jobOppId, @RequestBody JobIntakeData data) {
        // Find the job, then find it's intake id (if exists). If not exist, create.
        SalesforceJobOpp job = salesforceJobOppService.get(jobOppId); //TODO Note that we only currently have a method getting jobs by SalesforceID. Need to add a new method to get by job id.
        JobOppIntake intake = job.getJobOppIntake();
        if (intake == null) {
            intake = jobOppIntakeService.create(data);
            
            //EITHER...
            job.setJobOppIntake(intake);
            salesforceJobOppService.save(job);
            
            //OR - probably better. We have an example of this kind of thing in 
            salesforceJobOppService.assignIntake(job, intake);
            
            
        } else {
            jobOppIntakeService.update(intake.getId(), data);
        }
        
    }

    private DtoBuilder joiDto() {
        return new DtoBuilder()
            .add("id")
            .add("jobOpp", jobOppDto())
            .add("salaryRange")
            .add("recruitmentProcess")
            .add("employerCostCommitment")
            .add("location")
            .add("locationDetails")
            .add("benefits")
            .add("languageRequirements")
            .add("educationRequirements")
            .add("skillRequirements")
            .add("employmentExperience")
            .add("occupationCode")
            ;
    }

    private DtoBuilder jobOppDto() {
        return new DtoBuilder()
            .add("id")
            .add("sfId")
            .add("accepting")
            .add("contactEmail")
            .add("countryObject", countryDto())
            .add("createdDate")
            .add("employer")
            .add("name")
            ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }
}
