package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.search.CreateSavedSearchRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;


public interface SavedSearchService {

    Page<SavedSearch> searchSavedSearches(SearchSavedSearchRequest request);

    SearchCandidateRequest loadSavedSearch(long id);

    SavedSearch getSavedSearch(long id);

    SavedSearch createSavedSearch(CreateSavedSearchRequest request) throws EntityExistsException;

    SavedSearch updateSavedSearch(long id, UpdateSavedSearchRequest request) throws EntityExistsException;

    boolean deleteSavedSearch(long id);

}
