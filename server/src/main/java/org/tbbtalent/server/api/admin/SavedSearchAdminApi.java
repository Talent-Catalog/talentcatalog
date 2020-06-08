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
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.request.search.UpdateWatchingRequest;
import org.tbbtalent.server.service.SavedSearchService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/saved-search")
public class SavedSearchAdminApi implements 
        ITableApi<SearchSavedSearchRequest, 
                UpdateSavedSearchRequest, UpdateSavedSearchRequest> {

    private final SavedSearchService savedSearchService;

    /*
        Standard ITableApi methods
     */
    @Autowired
    public SavedSearchAdminApi(SavedSearchService savedSearchService) {
        this.savedSearchService = savedSearchService;
    }

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
    public @NotNull Map<String, Object> update(long id, @Valid UpdateSavedSearchRequest request) throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        SavedSearch savedSearch = this.savedSearchService.updateSavedSearch(id, request);
        return savedSearchDto().build(savedSearch);
    }
 
    /*
        End standard ITableApi methods
     */

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
                .add("statuses")
                .add("includeDraftAndDeleted")
                .add("gender")
                .add("occupationIds")
                .add("orProfileKeyword")
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
                .add("watcherUserIds")
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
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                .add("users", userDto())
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
