package org.tbbtalent.server.service;

import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.SavedList;
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

    /**
     * Returns a selection saved list for the given saved search and user,
     * creating one if necessary (there should only ever be one)
     * @param id Saved search ID
     * @param userId User id
     * @return A saved list to be used by the given user for selecting candidates
     * associated with the given saved list.
     * @throws NoSuchObjectException if there is no such saved search or user.
     */
    @NotNull SavedList getSelectionList(long id, Long userId) 
            throws NoSuchObjectException;
}
