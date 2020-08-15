package org.tbbtalent.server.api.admin;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.list.HasSetOfCandidatesImpl;
import org.tbbtalent.server.request.list.IHasSetOfCandidates;
import org.tbbtalent.server.request.search.ClearSelectionRequest;
import org.tbbtalent.server.request.search.SaveSelectionRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.SelectCandidateInSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.request.search.UpdateWatchingRequest;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/saved-search")
public class SavedSearchAdminApi implements 
        ITableApi<SearchSavedSearchRequest, 
                UpdateSavedSearchRequest, UpdateSavedSearchRequest> {

    private final CandidateService candidateService;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;
    private final SavedListBuilderSelector savedListBuilderSelector = new SavedListBuilderSelector();

    @Autowired
    public SavedSearchAdminApi(SavedSearchService savedSearchService,
                               SavedListService savedListService,
                               CandidateService candidateService) {
        this.candidateService = candidateService;
        this.savedListService = savedListService;
        this.savedSearchService = savedSearchService;
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
    public @NotNull Map<String, Object> get(long id) throws NoSuchObjectException {
        SavedSearch savedSearch = this.savedSearchService.getSavedSearch(id);
        return savedSearchDtoExtended().build(savedSearch);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(@Valid SearchSavedSearchRequest request) {
        Page<SavedSearch> savedSearches = this.savedSearchService.searchSavedSearches(request);
        return savedSearchDto().buildPage(savedSearches);
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

        //Get the selection list for this user and saved search.
        SavedList selectionList =
                savedSearchService.getSelectionList(id, request.getUserId());

        //Create an empty list request. This will clear the list.
        IHasSetOfCandidates listRequest = new HasSetOfCandidatesImpl();
        savedListService.replaceSavedList(selectionList.getId(), listRequest);
        
    }
    
    /**
     * Saves the given user's selections for the given saved search to either
     * the specified existing list, or to a newly created list.
     * The selections can be added to an existing list, or replace any existing
     * contents of the list.
     * @param id ID of saved search
     * @param request Request containing details of the list to be saved to,
     *                the associated user and whether or not the save should
     *                add ot or replace existing contents.
     * @return List which selection was saved to
     * @throws EntityExistsException If a new list needs to be created but the
     * list name already exists.
     * @throws InvalidRequestException if not authorized.
     * @throws NoSuchObjectException if there is no such saved search or user
     * with the given ids
     */
    @PutMapping("/save-selection/{id}")
    public Map<String, Object> saveSelection(@PathVariable("id") long id,
                              @Valid @RequestBody SaveSelectionRequest request)
            throws EntityExistsException, InvalidRequestException, NoSuchObjectException {

        //Get the selection list for this user and saved search.
        SavedList selectionList =
                savedSearchService.getSelectionList(id, request.getUserId());

        //Copy to the target list.
        SavedList targetList = 
                savedListService.copy(selectionList.getId(), request);

        return savedListBuilderSelector.selectBuilder().build(targetList);
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
        HasSetOfCandidatesImpl listRequest = new HasSetOfCandidatesImpl();
        listRequest.addCandidateId(request.getCandidateId());
        
        //Add or remove candidate depending on selection.
        if (request.isSelected()) {
            savedListService.mergeSavedList(selectionList.getId(), listRequest);
        } else {
            savedListService.removeFromSavedList(selectionList.getId(), listRequest);
        }
    }

    @GetMapping("/default")
    public @NotNull Map<String, Object> getDefault() {
        SavedSearch savedSearch = this.savedSearchService.getDefaultSavedSearch();
        return savedSearchDtoExtended().build(savedSearch);
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
        return savedSearchDtoExtended().build(savedSearch);
    }

    @PutMapping("/shared-remove/{id}")
    public Map<String, Object> removeSharedUser(
            @PathVariable("id") long id,
            @RequestBody UpdateSharingRequest request) {
        SavedSearch savedSearch = this.savedSearchService.removeSharedUser(id, request);
        return savedSearchDtoExtended().build(savedSearch);
    }

    @PutMapping("/watcher-add/{id}")
    public Map<String, Object> addWatcher(
            @PathVariable("id") long id,
            @RequestBody UpdateWatchingRequest request) {
        SavedSearch savedSearch = this.savedSearchService.addWatcher(id, request);
        return savedSearchDtoExtended().build(savedSearch);
    }

    @PutMapping("/watcher-remove/{id}")
    public Map<String, Object> removeWatcher(
            @PathVariable("id") long id,
            @RequestBody UpdateWatchingRequest request) {
        SavedSearch savedSearch = this.savedSearchService.removeWatcher(id, request);
        return savedSearchDtoExtended().build(savedSearch);
    }


    private DtoBuilder savedSearchNameDto() {
        return new DtoBuilder()
                .add("name")
                ;
    }

    private DtoBuilder savedSearchDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("savedSearchType")
                .add("savedSearchSubtype")
                .add("keyword")
                .add("simpleQueryString")
                .add("statuses")
                .add("includeDraftAndDeleted")
                .add("gender")
                .add("occupationIds")
                .add("minYrs")
                .add("maxYrs")
                .add("verifiedOccupationIds")
                .add("verifiedOccupationSearchType")
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
                .add("unRegistered")
                .add("fixed")
                .add("reviewable")
                .add("defaultSearch")
                .add("watcherUserIds")
                .add("createdBy", userDto())
                .add("users", userDto())
                ;
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

    public DtoBuilder savedSearchDtoExtended(){
        return savedSearchDto()
                .add("countryNames")
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
