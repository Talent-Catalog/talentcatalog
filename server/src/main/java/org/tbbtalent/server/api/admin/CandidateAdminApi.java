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

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate")
public class CandidateAdminApi {

    private final CandidateService candidateService;
    private final CandidateBuilderSelector builderSelector;
    private final CandidateIntakeDataBuilderSelector intakeDataBuilderSelector;

    @Autowired
    public CandidateAdminApi(CandidateService candidateService,
                             AuthService authService) {
        this.candidateService = candidateService;
        builderSelector = new CandidateBuilderSelector(authService);
        intakeDataBuilderSelector = new CandidateIntakeDataBuilderSelector(authService);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }
    
    @PostMapping("findbyemail")
    public Map<String, Object> findByCandidateEmail(@RequestBody CandidateEmailSearchRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping("findbynumberorname")
    public Map<String, Object> findByCandidateNumberOrName(@RequestBody CandidateNumberOrNameSearchRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping("findbyphone")
    public Map<String, Object> findByCandidatePhone(@RequestBody CandidatePhoneSearchRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @GetMapping("number/{number}")
    public Map<String, Object> get(@PathVariable("number") String number) {
        Candidate candidate = this.candidateService.findByCandidateNumber(number);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Candidate candidate = this.candidateService.getCandidate(id);
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
    
    @PostMapping
    public Map<String, Object> create(@RequestBody CreateCandidateRequest request) throws UsernameTakenException {
        Candidate candidate = this.candidateService.createCandidate(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/links")
    public Map<String, Object> updateLinks(@PathVariable("id") long id,
                            @RequestBody UpdateCandidateLinksRequest request) {
        Candidate candidate = this.candidateService.updateCandidateLinks(id, request);
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
        Candidate candidate = this.candidateService.updateCandidate(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/info")
    public Map<String, Object> updateAdditionalInfo(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = this.candidateService.updateCandidateAdditionalInfo(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/shareable-notes")
    public Map<String, Object> updateShareableNotes(@PathVariable("id") long id,
        @RequestBody UpdateCandidateShareableNotesRequest request) {
        Candidate candidate = this.candidateService.updateShareableNotes(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/shareable-docs")
    public Map<String, Object> updateShareableDocs(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateShareableDocsRequest request) {
        Candidate candidate = this.candidateService.updateShareableDocs(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("{id}/survey")
    public Map<String, Object> updateSurvey(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateSurveyRequest request) {
        Candidate candidate = this.candidateService.updateCandidateSurvey(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) {
        return this.candidateService.deleteCandidate(id);
    }

    @PostMapping(value = "export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(@RequestBody SearchCandidateRequest request,
                       HttpServletResponse response) throws IOException, ExportFailedException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        candidateService.exportToCsv(request, response.getWriter());
    }

    // todo: the name of this method seems strange?
    @GetMapping(value = "{id}/cv.pdf")
    public void downloadStudentListAsPdf(@PathVariable("id") long id, HttpServletResponse response)
            throws IOException {

        Candidate candidate = candidateService.getCandidate(id);
        String name = candidate.getUser().getDisplayName()+"-"+ "CV";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + name + ".pdf");

        Resource report = candidateService.generateCv(candidate);
        try (InputStream reportStream = report.getInputStream()) {
            IOUtils.copy(reportStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

    @PutMapping("{id}/create-folder")
    public Map<String, Object> createCandidateFolder(@PathVariable("id") long id) 
            throws IOException {
        Candidate candidate = this.candidateService.createCandidateFolder(id);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    /**
     * Creates a link to a Contact record on Salesforce for the given candidate.
     * <p/>
     * If no Contact record exists, one is created.
     *
     * @param id ID of candidate
     * @return Updated candidate object, containing link to corresponding 
     * Salesforce Contact record (created or
     * existing) in {@link Candidate#getSflink()}
     * @throws NoSuchObjectException if no candidate is found with that id
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @PutMapping("{id}/update-sf")
    public Map<String, Object> createUpdateSalesforce(@PathVariable("id") long id)
            throws NoSuchObjectException, GeneralSecurityException,
            WebClientException {
        Candidate candidate = candidateService.createUpdateSalesforce(id);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(candidate);
    }

    @PutMapping("update-sf")
    public void createUpdateSalesforce(@RequestBody UpdateCandidateOppsRequest request)
            throws GeneralSecurityException, WebClientException {
        candidateService.createUpdateSalesforce(request);
    }

    @PutMapping(value = "update-sf-by-list")
    public void createUpdateSalesforce(
        @Valid @RequestBody UpdateCandidateListOppsRequest request)
        throws NoSuchObjectException, GeneralSecurityException, WebClientException {

        candidateService.createUpdateSalesforce(request);
    }

    @PutMapping("{id}/intake")
    public void updateIntakeData(
            @PathVariable("id") long id, @RequestBody CandidateIntakeDataUpdate data) {
        candidateService.updateIntakeData(id, data);
    }

}
