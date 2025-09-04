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

package org.tctalent.server.api.admin;

import static org.tctalent.server.model.db.PartnerDtoHelper.employerDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.api.dto.DtoType;
import org.tctalent.server.api.dto.SavedListBuilderSelector;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.JobChatUserInfo;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.request.job.JobIntakeData;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.request.link.UpdateLinkRequest;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/admin/job")
@RequiredArgsConstructor
public class JobAdminApi implements
    ITableApi<SearchJobRequest, UpdateJobRequest, UpdateJobRequest> {

    private final CountryService countryService;
    private final SavedListBuilderSelector savedListBuilderSelector;
    private final JobService jobService;

    @Override
    @PostMapping
    public @NotNull Map<String, Object> create(@Valid UpdateJobRequest request)
        throws EntityExistsException {
        SalesforceJobOpp job = jobService.createJob(request);
        return jobDto().build(job);
    }

    @Override
    public @NotNull Map<String, Object> get(long id, DtoType dtoType) throws NoSuchObjectException {
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

    @PostMapping("check-unread-chats")
    public @NotNull JobChatUserInfo checkUnreadChats(
        @Valid @RequestBody SearchJobRequest request) {
        List<Long> oppIds = jobService.findUnreadChatsInOpps(request);
        JobChatUserInfo info = new JobChatUserInfo();
        info.setNumberUnreadChats(oppIds.size());
        return info;
    }

    @Override
    @PostMapping("search-paged")
    public @NotNull Map<String, Object> searchPaged(@Valid SearchJobRequest request) {
        Page<SalesforceJobOpp> jobs = jobService.searchJobs(request);
        final DtoBuilder builder =
            Boolean.TRUE.equals(request.getJobNameAndIdOnly()) ? jobNameAndIdDto() : jobDto();
        final Map<String, Object> objectMap = builder.buildPage(jobs);
        return objectMap;
    }

    @Override
    public @NotNull List<Map<String, Object>> search(@Valid SearchJobRequest request) {
        List<SalesforceJobOpp> jobs = jobService.searchJobsUnpaged(request);
        final DtoBuilder builder =
                Boolean.TRUE.equals(request.getJobNameAndIdOnly()) ? jobNameAndIdDto() : jobDto();
        return builder.buildList(jobs);
    }

    @Override
    @PutMapping("{id}")
    public @NotNull Map<String, Object> update(long id, UpdateJobRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateJob(id, request);
        return jobDto().build(job);
    }

    @PutMapping("{id}/jd-link")
    public @NotNull Map<String, Object> updateJdLink(
        @PathVariable("id") long id, @Valid @RequestBody UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateJdLink(id, updateLinkRequest);
        return jobDto().build(job);
    }

    @PutMapping("{id}/joi-link")
    public @NotNull Map<String, Object> updateJoiLink(
        @PathVariable("id") long id, @Valid @RequestBody UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateJoiLink(id, updateLinkRequest);
        return jobDto().build(job);
    }

    @PutMapping("{id}/interview-link")
    public @NotNull Map<String, Object> updateInterviewGuidanceLink(
        @PathVariable("id") long id, @Valid @RequestBody UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateInterviewGuidanceLink(id, updateLinkRequest);
        return jobDto().build(job);
    }

    @PutMapping("{id}/mou-link")
    public @NotNull Map<String, Object> updateMouLink(
            @PathVariable("id") long id, @Valid @RequestBody UpdateLinkRequest updateLinkRequest)
            throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.updateMouLink(id, updateLinkRequest);
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

    @PostMapping("{id}/upload/interview")
    public @NotNull Map<String, Object> uploadInterviewGuidance(
            @PathVariable("id") long id, @RequestParam("file") MultipartFile file)
            throws InvalidRequestException, IOException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.uploadInterviewGuidance(id, file);
        return jobDto().build(job);
    }

    @PostMapping("{id}/upload/mou")
    public @NotNull Map<String, Object> uploadMou(
            @PathVariable("id") long id, @RequestParam("file") MultipartFile file)
            throws InvalidRequestException, IOException, NoSuchObjectException {
        SalesforceJobOpp job = jobService.uploadMou(id, file);
        return jobDto().build(job);
    }

    private DtoBuilder jobNameAndIdDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("sfId")
            .add("contactUser", shortUserDto())
            .add("country", countryService.selectBuilder())
            .add("createdBy", shortUserDto())
            .add("createdDate")
            .add("employerEntity", employerDto())
            .add("evergreen")
            .add("hiringCommitment")
            .add("opportunityScore")
            .add("exclusionList", savedListBuilderSelector.selectBuilder())
            .add("jobSummary")
            .add("name")
            .add("nextStep")
            .add("nextStepDueDate")
            .add("publishedBy", shortUserDto())
            .add("publishedDate")
            .add("jobCreator", shortPartnerDto())
            .add("skipCandidateSearch")
            .add("stage")
            .add("starringUsers", shortUserDto())
            .add("submissionDueDate")
            .add("submissionList", savedListBuilderSelector.selectBuilder())
            .add("suggestedList", savedListBuilderSelector.selectBuilder())
            .add("suggestedSearches", savedSearchDto())
            .add("updatedBy", shortUserDto())
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

    private DtoBuilder shortUserDto() {
        return new DtoBuilder()
            .add("id")
            .add("firstName")
            .add("lastName")
            .add("email")
            ;
    }

    private DtoBuilder shortPartnerDto() {
        return new DtoBuilder()
            .add("id")
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
