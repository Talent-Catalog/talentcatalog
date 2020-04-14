package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.request.search.UpdateWatchingRequest;


public interface SavedSearchService {

    Page<SavedSearch> searchSavedSearches(SearchSavedSearchRequest request);

    SearchCandidateRequest loadSavedSearch(long id);

    SavedSearch getSavedSearch(long id);

    SavedSearch createSavedSearch(UpdateSavedSearchRequest request) throws EntityExistsException;

    SavedSearch updateSavedSearch(long id, UpdateSavedSearchRequest request) throws EntityExistsException;

    boolean deleteSavedSearch(long id);

    SavedSearch addSharedUser(long id, UpdateSharingRequest request);

    SavedSearch removeSharedUser(long id, UpdateSharingRequest request);

    SavedSearch addWatcher(long id, UpdateWatchingRequest request);

    SavedSearch removeWatcher(long id, UpdateWatchingRequest request);
}
