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

package org.tctalent.server.api.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ExportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tctalent.server.request.list.ContentUpdateType;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Web API for managing the association between a SavedList and its associated
 * Candidate's.
 * <p/>
 * This API:
 * <ul>
 *     <li>Updates the candidates which belong to a saved list</li>
 *     <li>Can request all candidates or a subset of candidates (based on
 *     search criteria) belonging to a saved list</li>
 * </ul>
 * <p/>
 * For actually modifying candidate details - see {@link CandidateAdminApi},
 * or for modifying saved list details see {@link SavedListAdminApi}.
 * <p/>
 * See also {@link CandidateSavedListAdminApi} which is the mirror image,
 * managing the reverse association between a Candidate and its associated
 * SavedList's (that it belongs to).
 *
 */
@RestController()
@RequestMapping("/api/admin/saved-list-candidate")
public class SavedListCandidateAdminApi implements
    IManyToManyApi<SavedListGetRequest, UpdateExplicitSavedListContentsRequest> {

    private final CandidateService candidateService;
    private final CandidateSavedListService candidateSavedListService;
    private final SavedListService savedListService;
    private final CandidateBuilderSelector candidateBuilderSelector;
    private final SavedListBuilderSelector savedListBuilderSelector = new SavedListBuilderSelector();
    @Autowired
    public SavedListCandidateAdminApi(
        CandidateService candidateService, CandidateOpportunityService candidateOpportunityService,
        CandidateSavedListService candidateSavedListService,
        SavedListService savedListService,
        UserService userService) {
        this.candidateService = candidateService;
        this.candidateSavedListService = candidateSavedListService;
        this.savedListService = savedListService;
        candidateBuilderSelector = new CandidateBuilderSelector(candidateOpportunityService, userService);
    }

    @GetMapping(value = "{id}/is-empty")
    public boolean isEmpty(@PathVariable("id") long savedListId) throws NoSuchObjectException {
        return savedListService.isEmpty(savedListId);
    }

    @Override
    public List<Map<String, Object>> list(long savedListId) throws NoSuchObjectException {
        SavedList savedList = savedListService.get(savedListId);
        Set<Candidate> candidates = savedList.getCandidates();
        DtoBuilder builder = candidateBuilderSelector.selectBuilder(DtoType.PREVIEW);
        return builder.buildList(candidates);
    }

    /**
     * This is where candidates are added to a list.
     * <p/>
     * It is a "merge" because duplicates are not allowed - ie a candidate cannot appear more than
     * once in a list. If a candidate is already in the list, they won't be added a second time.
     * @param savedListId List to be added to.
     * @param request Contains the ids of candidates to be added.
     * @throws NoSuchObjectException  If there is no list with that id
     */
    @Override
    public void merge(long savedListId, @Valid UpdateExplicitSavedListContentsRequest request)
            throws NoSuchObjectException {
        savedListService.mergeSavedList(savedListId, request);
    }

    /**
     * Merge the contents of the SavedList with the given id with the
     * candidates whose candidate numbers (NOT ids) appear in the given file.
     * @param savedListId ID of saved list to be updated
     * @param file File containing candidate numbers, one to a line
     * @throws NoSuchObjectException if there is no saved list with this id
     * or if any of the candidate numbers are not numeric or do not correspond to a candidate
     * @throws IOException If there is a problem reading the file
     */
    @PutMapping("{id}/merge-from-file")
    public void mergeFromFile(@PathVariable("id") long savedListId,
        @RequestParam("file") MultipartFile file) throws NoSuchObjectException, IOException {
        savedListService.mergeSavedListFromInputStream(savedListId, file.getInputStream());
    }

    @Override
    public void remove(long savedListId, @Valid UpdateExplicitSavedListContentsRequest request)
            throws NoSuchObjectException {
        savedListService.removeCandidateFromList(savedListId, request);
    }

    @Override
    public void replace(long savedListId, @Valid UpdateExplicitSavedListContentsRequest request)
            throws NoSuchObjectException {
        candidateSavedListService.clearSavedList(savedListId);
        savedListService.mergeSavedList(savedListId, request);
    }

    @Override
    public  @NotNull List<Map<String, Object>> search(
        long savedListId, @Valid SavedListGetRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListService.get(savedListId);

        List<Candidate> candidates = this.candidateService
            .getSavedListCandidatesUnpaged(savedList, request);

        savedListService.setCandidateContext(savedListId, candidates);

        DtoBuilder builder = candidateBuilderSelector.selectBuilder(request.getDtoType());
        return builder.buildList(candidates);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
            long savedListId, @Valid SavedListGetRequest request) throws NoSuchObjectException {
        SavedList savedList = savedListService.get(savedListId);

        Page<Candidate> candidates = this.candidateService
                .getSavedListCandidates(savedList, request);

        savedListService.setCandidateContext(savedListId, candidates);

        DtoBuilder builder = candidateBuilderSelector.selectBuilder(request.getDtoType());
        return builder.buildPage(candidates);
    }

    /**
     * Creates a new SavedList and initializes it's contents.
     * <p>
     *   If {@link UpdateExplicitSavedListContentsRequest#getCandidateIds()} is not null then
     *   it initializes the contents of the list, otherwise the list is
     *   initialized as empty.
     * </p>
     * @param request Request defining new list plus optional initial contents.
     * @return The details about the list - but not the contents.
     * @throws EntityExistsException if a list with this name already exists.
     */
    @PostMapping
    public @NotNull Map<String, Object> create(
        @Valid @RequestBody UpdateExplicitSavedListContentsRequest request) throws EntityExistsException {
        SavedList savedList = savedListService.createSavedList(request);

        //Now copy any contents across
        candidateSavedListService.copyContents(request, savedList);

        //Update all candidate statuses if requested.
        final UpdateCandidateStatusInfo info = request.getStatusUpdateInfo();
        if (info != null) {
            candidateService.updateCandidateStatus(savedList, info);
        }

        DtoBuilder builder = savedListBuilderSelector.selectBuilder();

        return builder.build(savedList);
    }

    @PostMapping(value = "{id}/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(
            @PathVariable("id") long savedListId,
            @Valid  @RequestBody SavedListGetRequest request,
            HttpServletResponse response) throws IOException, ExportFailedException {

        SavedList savedList = savedListService.get(savedListId);

        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        candidateService.exportToCsv(savedList, request, response.getWriter());
    }

    @PutMapping(value = "{id}/create-folders")
    public void createCandidateFolders(@PathVariable("id") long savedListId)
        throws NoSuchObjectException, IOException {
        SavedList savedList = savedListService.get(savedListId);
        Set<Candidate> candidates = savedList.getCandidates();
        Set<Long> candidateIds = candidates.stream().map(Candidate::getId).collect(Collectors.toSet());
        candidateService.createCandidateFolder(candidateIds);
    }

    /**
     * Adds or replaces the given candidates to a given existing list.
     * <p/>
     * Can have side effects as described in the request.
     * @param request Request defines the list to be updated, and the candidates to be added as well
     *                as other side effects.
     * @throws NoSuchObjectException If no list is specified or the list does not exist.
     */
    @PutMapping(value = "{id}/save-selection")
    public void saveSelection(@PathVariable("id") long savedListId,
        @Valid @RequestBody UpdateExplicitSavedListContentsRequest request) throws NoSuchObjectException {

        if (request.getUpdateType() == ContentUpdateType.replace) {
            candidateSavedListService.clearSavedList(savedListId);
        }
        savedListService.mergeSavedList(savedListId, request);

        final UpdateCandidateStatusInfo statusUpdateInfo =
            request.getStatusUpdateInfo();
        if (statusUpdateInfo != null) {
            UpdateCandidateStatusRequest ucsr =
                new UpdateCandidateStatusRequest(request.getCandidateIds());
            ucsr.setInfo(statusUpdateInfo);
            candidateService.updateCandidateStatus(ucsr);
        }
    }
}
