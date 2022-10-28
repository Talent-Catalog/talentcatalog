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

package org.tbbtalent.server.api.admin;

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/job")
public class JobAdminApi implements
    ITableApi<SearchJobRequest, UpdateJobRequest, UpdateJobRequest> {

    private final SavedListBuilderSelector savedListBuilderSelector = new SavedListBuilderSelector();

    private final JobService jobService;

    public JobAdminApi(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    @PostMapping
    public @NotNull Map<String, Object> create(@Valid UpdateJobRequest request)
        throws EntityExistsException {
        SalesforceJobOpp job = jobService.createJob(request);
        return jobDto().build(job);
    }

    @Override
    @GetMapping("{id}")
    public @NotNull Map<String, Object> get(long id) throws NoSuchObjectException {
        SalesforceJobOpp job = jobService.getJob(id);
        return jobDto().build(job);
    }

    @Override
    @PostMapping("search-paged")
    public @NotNull Map<String, Object> searchPaged(@Valid SearchJobRequest request) {
        Page<SalesforceJobOpp> jobs = jobService.searchJobs(request);
        final Map<String, Object> objectMap = jobDto().buildPage(jobs);
        return objectMap;
    }

    @Override
    @PutMapping("{id}")
    public @NotNull Map<String, Object> update(long id, UpdateJobRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateJob(id, request);
        return jobDto().build(job);
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("contactEmail")
            .add("contactUser", userDto())
            .add("country")
            .add("employer")
            .add("jobSummary")
            .add("name")
            .add("recruiterPartner", partnerDto())
            .add("stage")
            .add("submissionDueDate")
            .add("submissionList", savedListBuilderSelector.selectBuilder())
            .add("suggestedList", savedListBuilderSelector.selectBuilder())
            .add("suggestedSearches", savedSearchDto())
            ;
    }

    private DtoBuilder savedSearchDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
            .add("firstName")
            .add("lastName")
            .add("email")
            ;
    }

    private DtoBuilder partnerDto() {
        return new DtoBuilder()
            .add("name")
            .add("abbreviation")
            .add("websiteUrl")
            ;
    }
}
