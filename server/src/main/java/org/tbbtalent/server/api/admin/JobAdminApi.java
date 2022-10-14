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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Job;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/job")
public class JobAdminApi implements
    ITableApi<SearchJobRequest, UpdateJobRequest, UpdateJobRequest> {

    private final SavedListBuilderSelector builderSelector = new SavedListBuilderSelector();

    private final JobService jobService;

    public JobAdminApi(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public @NotNull Map<String, Object> create(@Valid UpdateJobRequest request)
        throws EntityExistsException {
        Job job = jobService.createJob(request);
        return jobDto().build(job);
    }

    @Override
    public @NotNull Map<String, Object> get(long id) throws NoSuchObjectException {
        Job job = jobService.getJob(id);
        return jobDto().build(job);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(@Valid SearchJobRequest request) {
        Page<Job> jobs = jobService.searchJobs(request);
        final Map<String, Object> objectMap = jobDto().buildPage(jobs);
        return objectMap;
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("country")
            .add("employer")
            .add("name")
            .add("stage")
            .add("submissionDueDate")
            .add("submissionList", builderSelector.selectBuilder())
            ;
    }
}
