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

import java.io.IOException;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.request.job.JobIntakeData;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.link.UpdateLinkRequest;
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

    @PostMapping("{id}/create-search")
    public @NotNull Map<String, Object> createSuggestedSearch(
        @PathVariable("id") long id, @Valid @RequestBody String suffix)
        throws NoSuchObjectException {
        SalesforceJobOpp job = jobService.createSuggestedSearch(id, suffix);
        return jobDto().build(job);
    }

    @PutMapping("{id}/intake")
    public void updateIntakeData(
            @PathVariable("id") long id, @RequestBody JobIntakeData data) {
        jobService.updateIntakeData(id, data);
    }

    @PutMapping("{id}/publish")
    public @NotNull Map<String, Object> publishJob(@PathVariable("id") long id)
        throws NoSuchObjectException {
        SalesforceJobOpp job = jobService.publishJob(id);
        return jobDto().build(job);
    }

    @PutMapping("{id}/remove-search")
    public @NotNull Map<String, Object> removeSuggestedSearch(
        @PathVariable("id") long id, @Valid @RequestBody long savedSearchId)
        throws NoSuchObjectException {
        SalesforceJobOpp job = jobService.removeSuggestedSearch(id, savedSearchId);
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

    @PutMapping("{id}/jdlink")
    public @NotNull Map<String, Object> updateJdLink(
        @PathVariable("id") long id, @Valid @RequestBody UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateJdLink(id, updateLinkRequest);
        return jobDto().build(job);
    }

    @PutMapping("{id}/joilink")
    public @NotNull Map<String, Object> updateJoiLink(
        @PathVariable("id") long id, @Valid @RequestBody UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateJoiLink(id, updateLinkRequest);
        return jobDto().build(job);
    }

    @PutMapping("{id}/starred")
    public @NotNull Map<String, Object> updateStarred(
        @PathVariable("id") long id, @Valid @RequestBody boolean starred)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateStarred(id, starred);
        return jobDto().build(job);
    }

    @PutMapping("{id}/summary")
    public @NotNull Map<String, Object> updateSummary(
        @PathVariable("id") long id, @Valid @RequestBody(required = false) String summary)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateJobSummary(id, summary);
        return jobDto().build(job);
    }

    @PostMapping("{id}/upload/jd")
    public @NotNull Map<String, Object> uploadJd(
        @PathVariable("id") long id, @RequestParam("file") MultipartFile file)
        throws InvalidRequestException, IOException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.uploadJd(id, file);
        return jobDto().build(job);
    }

    @PostMapping("{id}/upload/joi")
    public @NotNull Map<String, Object> uploadJoi(
        @PathVariable("id") long id, @RequestParam("file") MultipartFile file)
        throws InvalidRequestException, IOException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.uploadJoi(id, file);
        return jobDto().build(job);
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("sfId")
            .add("accepting")
            .add("contactEmail")
            .add("contactUser", userDto())
            .add("country")
            .add("createdBy", userDto())
            .add("createdDate")
            .add("employer")
            .add("hiringCommitment")
            .add("employerWebsite")
            .add("employerHiredInternationally")
            .add("hiringCommitment")
            .add("exclusionList", savedListBuilderSelector.selectBuilder())
            .add("jobSummary")
            .add("name")
            .add("publishedBy", userDto())
            .add("publishedDate")
            .add("recruiterPartner", partnerDto())
            .add("stage")
            .add("starringUsers", userDto())
            .add("submissionDueDate")
            .add("submissionList", savedListBuilderSelector.selectBuilder())
            .add("suggestedList", savedListBuilderSelector.selectBuilder())
            .add("suggestedSearches", savedSearchDto())
            .add("updatedBy", userDto())
            .add("updatedDate")
            .add("jobOppIntake", joiDto())
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
            .add("id")
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

    private DtoBuilder joiDto() {
        return new DtoBuilder()
            .add("id")
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
            .add("minSalary")
            .add("visaPathways")
            ;
    }
}
