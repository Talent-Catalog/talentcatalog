/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.request.candidate.CandidateEmailOrPhoneSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateExternalIdSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tbbtalent.server.request.candidate.DownloadCvRequest;
import org.tbbtalent.server.request.candidate.ResolveTaskAssignmentsRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateLinksRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateListOppsRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateMediaRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRegistrationRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateShareableDocsRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateShareableNotesRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tbbtalent.server.security.CandidateTokenProvider;
import org.tbbtalent.server.security.CvClaims;
import org.tbbtalent.server.service.db.CandidateOpportunityService;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate")
public class CandidateAdminApi {

    private final CandidateService candidateService;
    private final CandidateOpportunityService candidateOpportunityService;
    private final CandidateSavedListService candidateSavedListService;
    private final CandidateBuilderSelector builderSelector;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;
    private final CandidateIntakeDataBuilderSelector intakeDataBuilderSelector;
    private final CandidateTokenProvider candidateTokenProvider;

    @Autowired
    public CandidateAdminApi(CandidateService candidateService,
        CandidateOpportunityService candidateOpportunityService, CandidateSavedListService candidateSavedListService,
        SavedListService savedListService,
        SavedSearchService savedSearchService,
        UserService userService,
        CandidateTokenProvider candidateTokenProvider) {
        this.candidateService = candidateService;
        this.candidateOpportunityService = candidateOpportunityService;
        this.candidateSavedListService = candidateSavedListService;
        builderSelector = new CandidateBuilderSelector(userService);
        intakeDataBuilderSelector = new CandidateIntakeDataBuilderSelector();
        this.savedListService = savedListService;
        this.savedSearchService = savedSearchService;
        this.candidateTokenProvider = candidateTokenProvider;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateRequest request) {
        Page<Candidate> candidates = savedSearchService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping("findbyemail")
    public Map<String, Object> findByCandidateEmail(@RequestBody CandidateEmailSearchRequest request) {
        Page<Candidate> candidates = candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping("findbyemailorphone")
    public Map<String, Object> findByCandidateEmailOrPhone(@RequestBody CandidateEmailOrPhoneSearchRequest request) {
        Page<Candidate> candidates = candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping("findbynumberorname")
    public Map<String, Object> findByCandidateNumberOrName(@RequestBody CandidateNumberOrNameSearchRequest request) {
        Page<Candidate> candidates = candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping("findbyexternalid")
    public Map<String, Object> findByCandidateExternalId(@RequestBody CandidateExternalIdSearchRequest request) {
        Page<Candidate> candidates = candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @GetMapping("number/{number}")
    public Map<String, Object> get(@PathVariable("number") String number) {
        Candidate candidate = candidateService.findByCandidateNumberRestricted(number);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Candidate candidate = candidateService.getCandidate(id);

        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @GetMapping("{id}/intake")
    public Map<String, Object> getIntakeData(@PathVariable("id") long id) {
        Candidate candidate = candidateService.getCandidate(id);

        //Check if new TBB destinations have been added since the last time
        //someone looked at this candidate's intake.
        //If so, we automatically generate some new records corresponding tp
        //those new destinations - for example for candidate destination
        //preferences and visa checks.
        candidate = candidateService.addMissingDestinations(candidate);
        DtoBuilder builder = intakeDataBuilderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/links")
    public Map<String, Object> updateLinks(@PathVariable("id") long id,
                            @RequestBody UpdateCandidateLinksRequest request) {
        Candidate candidate = candidateService.updateCandidateLinks(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("status")
    public void updateStatus(@RequestBody UpdateCandidateStatusRequest request) {
        candidateService.updateCandidateStatus(request);
    }

    @PutMapping("{id}")
    public Map<String, Object> updateContactDetails(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateRequest request) {
        Candidate candidate = candidateService.updateCandidate(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/info")
    public Map<String, Object> updateAdditionalInfo(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = candidateService.updateCandidateAdditionalInfo(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/shareable-notes")
    public Map<String, Object> updateShareableNotes(@PathVariable("id") long id,
        @RequestBody UpdateCandidateShareableNotesRequest request) {
        Candidate candidate = candidateService.updateShareableNotes(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/shareable-docs")
    public Map<String, Object> updateShareableDocs(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateShareableDocsRequest request) {
        Candidate candidate = candidateSavedListService.updateShareableDocs(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/survey")
    public Map<String, Object> updateSurvey(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateSurveyRequest request) {
        Candidate candidate = candidateService.updateCandidateSurvey(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/media")
    public Map<String, Object> updateMedia(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateMediaRequest request) {
        Candidate candidate = candidateService.updateCandidateMedia(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/registration")
    public Map<String, Object> updateRegistration(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateRegistrationRequest request) {
        Candidate candidate = candidateService.updateCandidateRegistration(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) {
        return candidateService.deleteCandidate(id);
    }

    @PostMapping(value = "export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(@RequestBody SearchCandidateRequest request,
                       HttpServletResponse response) throws IOException, ExportFailedException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        savedSearchService.exportToCsv(request, response.getWriter());
    }

    @PostMapping(value = "{id}/cv.pdf")
    public void downloadCandidateCVPdf(@RequestBody DownloadCvRequest request, HttpServletResponse response)
            throws IOException {

        Candidate candidate = candidateService.getCandidate(request.getCandidateId());
        String name = candidate.getUser().getDisplayName()+"-"+ "CV";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + name + ".pdf");

        Resource report = candidateService.generateCv(candidate, request.getShowName(), request.getShowContact());
        try (InputStream reportStream = report.getInputStream()) {
            IOUtils.copy(reportStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

    @PutMapping("{id}/create-folder")
    public Map<String, Object> createCandidateFolder(@PathVariable("id") long id)
            throws IOException {
        Candidate candidate = candidateService.createCandidateFolder(id);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    /**
     * "Live" candidates are candidates who have been involved as potential candidates at least one
     * job.
     * Their details are pushed up to the Salesforce database so that we can track and report on
     * them using Salesforce's built-in functionality for that.
     * <p/>
     * Creates a link to a Contact record on Salesforce for the given candidate.
     * <p/>
     * If no Contact record exists, one is created.
     *
     * @param id ID of candidate
     * @return Updated candidate object, containing link to corresponding
     * Salesforce Contact record (created or
     * existing) in {@link Candidate#getSflink()}
     * @throws NoSuchObjectException if no candidate is found with that id
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @PutMapping("{id}/update-live")
    public Map<String, Object> createUpdateLiveCandidate(@PathVariable("id") long id)
            throws NoSuchObjectException, SalesforceException,
            WebClientException {
        Candidate candidate = candidateService.createUpdateSalesforce(id);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("update-opps")
    public void createUpdateOppsFromCandidates(@RequestBody UpdateCandidateOppsRequest request)
            throws WebClientException {
        candidateOpportunityService.createUpdateCandidateOpportunities(request);
    }

    @PutMapping(value = "update-opps-by-list")
    public void createUpdateOppsFromCandidateList(
        @Valid @RequestBody UpdateCandidateListOppsRequest request)
        throws NoSuchObjectException, SalesforceException, WebClientException {

        savedListService.createUpdateSalesforce(request);
    }

    @PutMapping("{id}/intake")
    public void updateIntakeData(
            @PathVariable("id") long id, @RequestBody CandidateIntakeDataUpdate data) {
        candidateService.updateIntakeData(id, data);
    }

    @PutMapping("resolve-tasks")
    public void resolveOutstandingTasks(@RequestBody ResolveTaskAssignmentsRequest request) {
        candidateService.resolveOutstandingTaskAssignments(request);
    }

    @GetMapping(value = "/token/{cn}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateToken(@PathVariable("cn") String candidateNumber,
                                @RequestParam(defaultValue = "false") boolean restrictCandidateOccupations,
                                @RequestParam(defaultValue = "") List<Long> candidateOccupationIds) {
         CvClaims cvClaims = new CvClaims(candidateNumber, restrictCandidateOccupations, candidateOccupationIds);
         String token = candidateTokenProvider.generateCvToken(cvClaims, 365L);
         return token;
    }

}
