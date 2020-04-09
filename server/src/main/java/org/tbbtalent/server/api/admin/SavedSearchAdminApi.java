package org.tbbtalent.server.api.admin;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.service.SavedSearchService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/saved-search")
public class SavedSearchAdminApi {

    private final SavedSearchService savedSearchService;

    @Autowired
    public SavedSearchAdminApi(SavedSearchService savedSearchService) {
        this.savedSearchService = savedSearchService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchSavedSearchRequest request) {
        Page<SavedSearch> savedSearches = this.savedSearchService.searchSavedSearches(request);
        return savedSearchDto().buildPage(savedSearches);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        SavedSearch savedSearch = this.savedSearchService.getSavedSearch(id);
        return savedSearchDtoExtended().build(savedSearch);
    }

    @GetMapping("{id}/load")
    public SearchCandidateRequest load(@PathVariable("id") long id) {
        return this.savedSearchService.loadSavedSearch(id);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody UpdateSavedSearchRequest request) throws EntityExistsException {
        SavedSearch savedSearch = this.savedSearchService.createSavedSearch(request);
        return savedSearchDto().build(savedSearch);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateSavedSearchRequest request) throws EntityExistsException  {
        SavedSearch savedSearch = this.savedSearchService.updateSavedSearch(id, request);
        return savedSearchDto().build(savedSearch);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.savedSearchService.deleteSavedSearch(id);
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

    private DtoBuilder savedSearchNameDto() {
        return new DtoBuilder()
                .add("name")
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
