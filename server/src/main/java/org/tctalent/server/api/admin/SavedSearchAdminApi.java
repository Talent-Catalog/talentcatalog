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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.IdsRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.request.search.ClearSelectionRequest;
import org.tctalent.server.request.search.CreateFromDefaultSavedSearchRequest;
import org.tctalent.server.request.search.SearchSavedSearchRequest;
import org.tctalent.server.request.search.SelectCandidateInSearchRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;
import org.tctalent.server.request.search.UpdateWatchingRequest;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/admin/saved-search")
@Slf4j
public class SavedSearchAdminApi implements
        ITableApi<SearchSavedSearchRequest,
            UpdateSavedSearchRequest, UpdateSavedSearchRequest> {

    private final CandidateService candidateService;
    private final CandidateSavedListService candidateSavedListService;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;
    private final SavedListBuilderSelector savedListBuilderSelector = new SavedListBuilderSelector();
    private final ExportColumnsBuilderSelector exportColumnsBuilderSelector
        = new ExportColumnsBuilderSelector();


    @Autowired
    public SavedSearchAdminApi(SavedSearchService savedSearchService,
        SavedListService savedListService,
        CandidateService candidateService,
        CandidateSavedListService candidateSavedListService) {
        this.candidateService = candidateService;
        this.savedListService = savedListService;
        this.savedSearchService = savedSearchService;
        this.candidateSavedListService = candidateSavedListService;
    }

    /*
        Standard ITableApi methods
     */
    @Override
    public @NotNull Map<String, Object> create(@Valid UpdateSavedSearchRequest request) throws EntityExistsException {
        SavedSearch savedSearch = this.savedSearchService.createSavedSearch(request);
        return savedSearchDto().build(savedSearch);
    }

    @Override
    public boolean delete(long id) throws EntityReferencedException, InvalidRequestException {
        return this.savedSearchService.deleteSavedSearch(id);
    }

    @Override
    public @NotNull Map<String, Object> get(long id, DtoType dtoType) {
        LogBuilder.builder(log)
            .searchId(id)
            .action("GetSavedSearch")
            .message("Getting " + dtoType + " saved search")
            .logInfo();

        SavedSearch savedSearch = savedSearchService.getSavedSearch(id);
        DtoBuilder dtoBuilder = selectedDtoBuilder(dtoType);
        return dtoBuilder.build(savedSearch);
    }

    @Override
    public @NotNull List<Map<String, Object>> search(@Valid SearchSavedSearchRequest request) {
        List<SavedSearch> savedSearches = savedSearchService.search(request);
        return savedSearchDto().buildList(savedSearches);
    }

    @PostMapping("search-ids")
    @NotNull List<Map<String, Object>> searchByIds(@Valid @RequestBody IdsRequest request) {
        List<SavedSearch> savedSearches = savedSearchService.search(request);
        return savedSearchDto().buildList(savedSearches);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(@Valid SearchSavedSearchRequest request) {
        Page<SavedSearch> savedSearches = this.savedSearchService.searchPaged(request);
        DtoBuilder builder = selectedDtoBuilder(request);
        return builder.buildPage(savedSearches);
    }

    @Override
    public @NotNull Map<String, Object> update(long id, @Valid UpdateSavedSearchRequest request)
            throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        SavedSearch savedSearch = this.savedSearchService.updateSavedSearch(id, request);
        return savedSearchDto().build(savedSearch);
    }

    /*
        End standard ITableApi methods
     */

    /**
     * Clears the given user's selections for the given saved search.
     * @param id ID of saved search
     * @param request Request containing the associated user.
     * @throws InvalidRequestException if not authorized.
     * @throws NoSuchObjectException if there is no such saved search
     */
    @PutMapping("/clear-selection/{id}")
    public void clearSelection(@PathVariable("id") long id,
                               @Valid @RequestBody ClearSelectionRequest request)
            throws InvalidRequestException, NoSuchObjectException {

        savedSearchService.clearSelection(id, request.getUserId());
    }

    /**
     * Creates a new saved search from the current user's default saved search,
     * named as specified in the request (either with the name of a specified
     * existing saved list, or with a specified name) and with the sfJoblink,
     * if any, in the request.
     * <p/>
     * The selection for the new saved search is the same as the selection
     * (including any context notes) for the default search.
     * <p/>
     * If a saved search with the given already exists, it is replaced.
     * @param request Request containing details from which the search is created.
     * @return Created search
     * @throws NoSuchObjectException If there is no logged in user or no list
     * with given id.
     */
    @PostMapping("create-from-default")
    public @NotNull Map<String, Object> createFromDefaultSearch(
            @Valid @RequestBody CreateFromDefaultSavedSearchRequest request)
            throws NoSuchObjectException {
        SavedSearch savedSearch = this.savedSearchService
                .createFromDefaultSavedSearch(request);
        return savedSearchDto().build(savedSearch);
    }

    /**
     * Saves the given user's selections for the given saved search to either
     * the specified existing list, or to a newly created list if existing list
     * not specified.
     * The selections can be added to an existing list, or replace any existing
     * contents of the list.
     * @param id ID of saved search
     * @param request Request containing details of the list to be saved to,
     *                the associated user and whether or not the save should
     *                add to or replace existing contents.
     * @return Representation of List which selection was saved to
     * @throws EntityExistsException If a new list needs to be created but the
     * list name already exists.
     * @throws InvalidRequestException if not authorized.
     * @throws NoSuchObjectException if there is no such saved search or user
     * with the given ids
     */
    @PutMapping("/save-selection/{id}")
    public Map<String, Object> saveSelection(@PathVariable("id") long id,
                              @Valid @RequestBody CopySourceContentsRequest request)
            throws EntityExistsException, InvalidRequestException, NoSuchObjectException {

        //Get the selection list for this user and saved search.
        SavedList selectionList = savedSearchService.getSelectionListForLoggedInUser(id);

        //Copy to the target list.
        SavedList targetList = candidateSavedListService.copy(selectionList, request);

        //Update all candidate statuses if requested.
        final UpdateCandidateStatusInfo info = request.getStatusUpdateInfo();
        if (info != null) {
            candidateService.updateCandidateStatus(selectionList, info);
        }

        return savedListBuilderSelector.selectBuilder().build(targetList);
    }

    /**
     * Updates the status of each selected candidate in the logged in user's selection for the
     * given saved search.
     * <p/>
     * The status and related information are specified in the given request.
     * @param id ID of saved search
     * @param request Request containing the new status and other information relating to the
     *                status change.
     * @throws InvalidRequestException if not authorized
     * @throws NoSuchObjectException if there is no such saved search or user
     * with the given ids
     * @throws InvalidSessionException if no user is logged in
     */
    @PutMapping("/update-selected-statuses/{id}")
    public void updateSelectedStatuses(@PathVariable("id") long id,
                              @Valid @RequestBody UpdateCandidateStatusInfo request)
            throws InvalidRequestException, NoSuchObjectException, InvalidSessionException {

        //Get the selection list for this user and saved search.
        SavedList selectionList = savedSearchService.getSelectionListForLoggedInUser(id);

        UpdateCandidateStatusRequest ucsr = new UpdateCandidateStatusRequest();
        List<Long> candidateIds =
            selectionList.getCandidates().stream().map(Candidate::getId).collect(Collectors.toList());
        ucsr.setCandidateIds(candidateIds);
        ucsr.setInfo(request);
        candidateService.updateCandidateStatus(ucsr);
    }

    /**
     * Returns the number of candidates in the logged in user's selection for the
     * given saved search.
     *
     * @param id ID of saved search
     * @return Number of candidates selected by logged in user
     * @throws NoSuchObjectException if there is no such saved search.
     * @throws InvalidSessionException if there is no logged in user.
     */
    @GetMapping("/get-selection-count/{id}")
    public long getSelectionCount(@PathVariable("id") long id)
        throws NoSuchObjectException, InvalidSessionException {
        SavedList selectionList = savedSearchService.getSelectionListForLoggedInUser(id);
        return selectionList.getCandidates().size();
    }

    /**
     * Update the record with the given id from the data in the given request.
     * @param id ID of saved search
     * @param request Request contains the user making the selection and the
     *                candidate being selected.
     * @throws InvalidRequestException if not authorized to update this record.
     * @throws NoSuchObjectException if there is no such saved search, user
     * or candidate with the given ids
     */
    @PutMapping("/select-candidate/{id}")
    public void selectCandidate(@PathVariable("id") long id,
                         @Valid @RequestBody SelectCandidateInSearchRequest request)
            throws InvalidRequestException, NoSuchObjectException {

        //Get the selection list for this user and saved search.
        SavedList selectionList =
                savedSearchService.getSelectionList(id, request.getUserId());

        //Create a list request containing our candidate id.
        UpdateExplicitSavedListContentsRequest listRequest = new UpdateExplicitSavedListContentsRequest();
        listRequest.addCandidateId(request.getCandidateId());

        //Add or remove candidate depending on selection.
        if (request.isSelected()) {
            savedListService.mergeSavedList(selectionList.getId(), listRequest);
        } else {
            savedListService.removeCandidateFromList(selectionList.getId(), listRequest);
        }
    }

    @GetMapping("/default")
    public @NotNull Map<String, Object> getDefault() {
        SavedSearch savedSearch = this.savedSearchService.getDefaultSavedSearch();
        return extendedSavedSearchDto().build(savedSearch);
    }

    @GetMapping("{id}/load")
    public SearchCandidateRequest load(@PathVariable("id") long id) {
        return this.savedSearchService.loadSavedSearch(id);
    }

    @PutMapping("/shared-add/{id}")
    public Map<String, Object> addSharedUser(
            @PathVariable("id") long id,
            @RequestBody UpdateSharingRequest request) {
        SavedSearch savedSearch = this.savedSearchService.addSharedUser(id, request);
        return extendedSavedSearchDto().build(savedSearch);
    }

    @PutMapping("/shared-remove/{id}")
    public Map<String, Object> removeSharedUser(
            @PathVariable("id") long id,
            @RequestBody UpdateSharingRequest request) {
        SavedSearch savedSearch = this.savedSearchService.removeSharedUser(id, request);
        return extendedSavedSearchDto().build(savedSearch);
    }

    @PutMapping("/watcher-add/{id}")
    public Map<String, Object> addWatcher(
            @PathVariable("id") long id,
            @RequestBody UpdateWatchingRequest request) {
        SavedSearch savedSearch = this.savedSearchService.addWatcher(id, request);
        return extendedSavedSearchDto().build(savedSearch);
    }

    @PutMapping("/watcher-remove/{id}")
    public Map<String, Object> removeWatcher(
            @PathVariable("id") long id,
            @RequestBody UpdateWatchingRequest request) {
        SavedSearch savedSearch = this.savedSearchService.removeWatcher(id, request);
        return extendedSavedSearchDto().build(savedSearch);
    }

    @PutMapping("/context/{id}")
    public void updateContextNote(
            @PathVariable("id") long id,
            @RequestBody UpdateCandidateContextNoteRequest request) {
        savedSearchService.updateCandidateContextNote(id, request);
    }

    @PutMapping("/description/{id}")
    public void updateDescription(
        @PathVariable("id") long id,
        @RequestBody UpdateCandidateSourceDescriptionRequest request) {
        savedSearchService.updateDescription(id, request);
    }

    @PutMapping("/displayed-fields/{id}")
    public void updateDisplayedFieldPaths(
            @PathVariable("id") long id,
            @RequestBody UpdateDisplayedFieldPathsRequest request) {
        savedSearchService.updateDisplayedFieldPaths(id, request);
    }

    private DtoBuilder selectedDtoBuilder(SearchSavedSearchRequest request) {
        return selectedDtoBuilder(request.getDtoType());
    }

    private DtoBuilder selectedDtoBuilder(DtoType dtoType) {
        if (dtoType == null) {
            return savedSearchDto(); // Default behavior if dtoType is null
        }

        return switch (dtoType) {
            case MINIMAL -> minimalSavedSearchDto();
            case EXTENDED -> extendedSavedSearchDto();
            default -> savedSearchDto();
        };
    }

    private DtoBuilder savedSearchNameDto() {
        return new DtoBuilder()
                .add("name")
                ;
    }

    private DtoBuilder minimalSavedSearchDto() {
        return new DtoBuilder()
            .add("id")
            .add("publicId")
            .add("name")
            .add("savedSearchType")
            .add("savedSearchSubtype")
            .add("displayedFieldsLong")
            .add("displayedFieldsShort")
            .add("users", userDto())
            .add("watcherUserIds")
            .add("createdBy", userDto())
            ;
    }

    private DtoBuilder savedSearchDto() {
        return new DtoBuilder()
                .add("id")
                .add("publicId")
                .add("description")
                .add("displayedFieldsLong")
                .add("displayedFieldsShort")
                .add("exportColumns", exportColumnsBuilderSelector.selectBuilder())
                .add("name")
                .add("savedSearchType")
                .add("savedSearchSubtype")
                .add("keyword")
                .add("simpleQueryString")
                .add("statuses")
                .add("gender")
                .add("occupationIds")
                .add("minYrs")
                .add("maxYrs")
                .add("partnerIds")
                .add("nationalityIds")
                .add("nationalitySearchType")
                .add("countryIds")
                .add("englishMinWrittenLevel")
                .add("englishMinSpokenLevel")
                .add("otherLanguage", languageDto())
                .add("otherMinWrittenLevel")
                .add("otherMinSpokenLevel")
                .add("lastModifiedFrom")
                .add("lastModifiedTo")
                .add("createdFrom")
                .add("createdTo")
                .add("minAge")
                .add("maxAge")
                .add("minEducationLevel")
                .add("educationMajorIds")
                .add("fixed")
                .add("reviewable")
                .add("global")
                .add("defaultSearch")
                .add("includePendingTermsCandidates")
                .add("miniIntakeCompleted")
                .add("fullIntakeCompleted")
                .add("sfJobOpp", jobOppIdsDto())
                .add("watcherUserIds")
                .add("createdBy", userDto())
                .add("users", userDto())
                ;
    }

    public DtoBuilder extendedSavedSearchDto() {
        return savedSearchDto()
            .add("countryNames")
            .add("partnerNames")
            .add("nationalityNames")
            .add("vettedOccupationNames")
            .add("occupationNames")
            .add("educationMajors")
            .add("englishWrittenLevel")
            .add("englishSpokenLevel")
            .add("otherWrittenLevel")
            .add("otherSpokenLevel")
            .add("minEducationLevelName")
            .add("createdDate")
            .add("updatedBy", userDto())
            .add("updatedDate")
            .add("searchJoins", searchJoinDto());

    }

    private DtoBuilder jobOppIdsDto() {
        return new DtoBuilder()
            .add("id")
            .add("sfId")
            ;
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

    private DtoBuilder searchJoinDto() {
        return new DtoBuilder()
                .add("childSavedSearch", savedSearchNameDto())
                .add("searchType")
                ;
    }

    private DtoBuilder userDto() {
        return  new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }

}
