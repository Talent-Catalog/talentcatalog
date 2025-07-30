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

package org.tctalent.server.service.db;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.RegisteredListException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.IdsRequest;
import org.tctalent.server.request.candidate.PublishListRequest;
import org.tctalent.server.request.candidate.PublishedDocImportReport;
import org.tctalent.server.request.candidate.UpdateCandidateListOppsRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.link.UpdateShortNameRequest;
import org.tctalent.server.request.list.IHasSetOfCandidates;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.request.list.UpdateSavedListContentsRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;

/**
 * Saved List Service
 *
 * @author John Cameron
 */
public interface SavedListService {

    /**
     * Add the given candidate to the given destination list - merging it in with any
     * existing candidates in the list (no duplicates - if a candidate is
     * already present it will still only appear once).
     * <p/>
     * If a contextNote is supplied it will be added with the candidate.
     * @param destinationList List to which candidates are added
     * @param candidate Candidate to add to the destination list
     * @param contextNote Context note associated with candidate in this list.
     */
    void addCandidateToList(@NonNull SavedList destinationList, @NonNull Candidate candidate,
        @Nullable String contextNote);

    /**
     * Add the given candidate to the given destination list - merging it in with any
     * existing candidates in the list (no duplicates - if a candidate is
     * already present it will still only appear once).
     * <p/>
     * If a source list is supplied, the original candidate context will be
     * copied across (eg contextNote).
     * @param destinationList List to which candidates are added
     * @param candidate Candidate to add to the destination list
     * @param sourceList If not null, refers to the list where candidates came
     *                   from, so that context can be copied across.
     */
    void addCandidateToList(@NonNull SavedList destinationList, @NonNull Candidate candidate,
        @Nullable SavedList sourceList);

    /**
     * See {@link #addCandidateToList} - except that this adds multiple candidates
     */
    void addCandidatesToList(@NonNull SavedList destinationList, @NonNull Iterable<Candidate> candidates,
        @Nullable SavedList sourceList);

    /**
     * Remove the given candidate from the given savedList
     * @param candidate Candidate to remove
     * @param savedList SavedList to remove from
     */
    void removeCandidateFromList(@NonNull Candidate candidate, @NonNull SavedList savedList);

    /**
     * Remove the candidates indicated in the given request from the SavedList
     * with the given id.
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the new list contents
     * @throws NoSuchObjectException if there is no saved list with this id
     */
    void removeCandidateFromList(long savedListId, UpdateExplicitSavedListContentsRequest request)
        throws NoSuchObjectException;

    /**
     * Associates the given task with the given list. Also assigns that task to all candidates
     * currently in the list.
     * <p/>
     * Once the task has been associated with a list, any future candidates added to that list
     * will automatically be assigned that task (unless they have already been assigned the task).
     * <p/>
     * A user may want to assign a task to a list of candidates. For example if there’s a list of
     * candidates shortlisted for a job opportunity, they might all be required to complete some
     * pre-offer tasks via a task list.
     * <p/>
     * A new active TaskAssignment object is created for each candidate in the list, associated with
     * the given task.
     *
     * @param user    - User who is associating task with list
     * @param task    - Task to be associated with the list, and assigned to candidates
     * @param list    - List of candidates to whom the task should be assigned
     */
    void associateTaskWithList(User user, TaskImpl task, SavedList list);

    /**
     * Removes the association of the given task with the given list.
     * Also deactivates any active incomplete candidate assignments of that task which are related
     * to this list.
     *
     * @param user    - User who is removing the association
     * @param task    - Task to be deassociated
     * @param list    - List in question
     */
    void deassociateTaskFromList(User user, TaskImpl task, SavedList list);

    /**
     * Creates a folder for the given list on Google Drive with a subfolder for Job Description docs.
     * <p/>
     * If a folder already exists for the list, does nothing.
     *
     * @param id ID of list
     * @return Updated saved list object, containing link to folder (created or
     * existing) in {@link SavedList#getFolderlink()} and also link to the subfolder
     * {@link SavedList#getFolderjdlink()}
     * @throws NoSuchObjectException if no list is found with that id
     * @throws IOException           if there is a problem creating the folder.
     */
    SavedList createListFolder(long id)
        throws NoSuchObjectException, IOException;

    /**
     * Creates a new SavedList unless it is a registered list and a registered list for that
     * job, as defined by {@link SavedList#getSfJobOpp()}, already exists, in which case
     * nothing new is created, and the existing list is returned.
     * @param request Request defining new list (including whether it is a registered list
     *                ({@link UpdateSavedListInfoRequest#getRegisteredJob()})
     * @return Created saved list
     * @throws EntityExistsException if a list with this name already exists.
     * @throws RegisteredListException if request is for a registered list but sfJoblink or name is missing
     */
    SavedList createSavedList(UpdateSavedListInfoRequest request)
            throws EntityExistsException, RegisteredListException;

    /**
     * Creates a new SavedList unless it is a registered list and a registered list for that
     * job, as defined by {@link SavedList#getSfJobOpp()} already exists, in which case
     * nothing new is created, and the existing list is returned.
     * @param user User to be recorded as creator of saved list
     * @param request Request defining new list (including whether it is a registered list
     *                ({@link UpdateSavedListInfoRequest#getRegisteredJob()})
     * @return Created saved list
     * @throws EntityExistsException if a list with this name already exists.
     * @throws RegisteredListException if request is for a registered list but sfJoblink or name is missing
     */
    SavedList createSavedList(User user, UpdateSavedListInfoRequest request)
        throws EntityExistsException, RegisteredListException;

    /**
     * Creates/updates Salesforce records corresponding to candidates in a given list
     * <p/>
     * This could involve creating or updating contact records and/or
     * creating or updating opportunity records.
     * <p/>
     * Salesforce links may be created and stored in candidate records.
     *
     * @param request Identifies list of candidates as well as optional Salesforce fields to set on
     *                candidate opportunities
     * @throws NoSuchObjectException  if there is no saved list with this id
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    void createUpdateSalesforce(UpdateCandidateListOppsRequest request)
        throws NoSuchObjectException, SalesforceException, WebClientException;

    /**
     * Fetches the candidates specified in the given request.
     * @param request Request containing candidates to be fetched
     * @return Set of candidates - never null. Empty if no candidates in request.
     * @throws NoSuchObjectException if a candidate in the request does not exist
     */
    @NonNull
    Set<Candidate> fetchCandidates(IHasSetOfCandidates request)
        throws NoSuchObjectException;

    /**
     * Fetches the ids of candidates in the given list
     * @param listId Id of list
     * @return Ids of candidates contained in the list
     */
    @NonNull
    Set<Long> fetchCandidateIds(long listId);

    /**
     * Fetches the public ids of candidates in the given list, note that the list id provided
     * must be the public id of the list, not the internal id.
     *
     * @param publicListId public Id of list
     * @return public Ids of candidates contained in the list
     */
    @NonNull
    Set<String> fetchCandidatePublicIds(String publicListId);

    /**
     * Fetches the ids of the union of all candidates in all the given lists
     * @param listIds Ids of lists
     * @return Ids of candidates contained in the lists or null if ids is null
     */
    @Nullable
    Set<Long> fetchUnionCandidateIds(@Nullable List<Long> listIds);

    /**
     * Fetches the public ids of the union of all candidates in all the given lists, note that the
     * list ids provided must be the public ids of the lists, not the internal ids.
     *
     * @param publicListIds public ids of saved lists
     * @return public ids of candidates contained in the lists, or null if input is null
     */
    @Nullable
    Set<String> fetchUnionCandidatePublicIds(@Nullable List<String> publicListIds);

    /**
     * Fetches the ids of candidates which appear in all the given lists
     * @param listIds Ids of lists
     * @return Ids of candidates contained in the lists or null if ids is null
     */
    @Nullable
    Set<Long> fetchIntersectionCandidateIds(@Nullable List<Long> listIds);

    /**
     * Fetches the public ids of the intersection of all candidates in all the given lists, note
     * that the list ids provided must be the public ids of the lists, not the internal ids.
     *
     * @param publicListIds public ids of saved lists
     * @return public ids of candidates common to all the lists, or null if input is null
     */
    @Nullable
    Set<String> fetchIntersectionCandidatePublicIds(@Nullable List<String> publicListIds);

    /**
     * Get the SavedList with the given id.
     * @param savedListId ID of SavedList to get
     * @return Saved list
     * @throws NoSuchObjectException if there is no saved list with this id.
     */
    @NonNull
    SavedList get(long savedListId) throws NoSuchObjectException;

    /**
     * Get the SavedList, if any, with the given name (ignoring case), owned by the given user.
     * @param user Owner of list
     * @param listName Name of list (case insensitive - eg "test" will match "Test")
     * @return Saved list or null if not found
     */
    @Nullable
    SavedList get(@NonNull User user, String listName);

    /**
     * Returns true if there are no candidates in the list
     * @param id ID of list
     * @return True if no candidates in list
     * @throws NoSuchObjectException if there is no such saved list
     */
    boolean isEmpty(long id) throws NoSuchObjectException;

    /**
     * Return all SavedList's associated with the given candidate that match
     * the given request, ordered by name.
     * <p/>
     * See also {@link #search} which does the same except for
     * any candidate.
     *
     * @param candidateId Candidate whose lists we are searching
     * @param request Defines which SavedList's to return
     * @return Matching SavedList's
     */
    List<SavedList> search(long candidateId, SearchSavedListRequest request);

    /**
     * Return all SavedList's that match the given ids, ordered by name.
     * @param request Defines the ids of the SavedList's to return
     * @return Requested SavedList's
     */
    List<SavedList> search(IdsRequest request);

    /**
     * Return all SavedList's that match the given request, ordered by name.
     * <p/>
     * See also {@link #searchPaged} which does the same except
     * returns just one page of results.
     * @param request Defines which SavedList's to return
     * @return Matching SavedList's
     */
    List<SavedList> search(SearchSavedListRequest request);

    /**
     * This is how candidates are added to a list.
     * <p/>
     * Merge the contents of the SavedList with the given id with the
     * candidates indicated in the given request.
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the contents to be merged into the list
     * @throws NoSuchObjectException if there is no saved list with this id
     */
    void mergeSavedList(long savedListId, UpdateExplicitSavedListContentsRequest request)
        throws NoSuchObjectException;

    /**
     * Merge the contents of the SavedList with the given id with the
     * candidates whose candidate numbers (NOT ids) appear in the given input stream.
     * @param savedListId ID of saved list to be updated
     * @param is Input stream containing candidate numbers, one to a line
     * @throws NoSuchObjectException if there is no saved list with this id
     * or if any of the candidate numbers are not numeric or do not correspond to a candidate
     * @throws IOException If there is a problem reading the input stream
     */
    void mergeSavedListFromInputStream(long savedListId, InputStream is)
        throws NoSuchObjectException, IOException;

    /**
     * Return a page of SavedList's that match the given request, ordered by
     * name.
     * <p/>
     * See also {@link #search} which does the same except it
     * returns all matching results.
     * @param request Defines which SavedList's to return
     * @return Matching SavedList's
     */
    Page<SavedList> searchPaged(SearchSavedListRequest request);

    /**
     * Mark the given Candidate objects with the given list context.
     * This means that context fields (ie ContextNote) associated with the
     * list will be returned through {@link Candidate#getContextNote()}
     * @param savedListId ID of saved list
     * @param candidates Candidate objects to be marked with the list context. Note that this
     *                   is a transient property only found on the given objects (ie it is not
     *                   stored in the database).
     */
    void setCandidateContext(long savedListId, Iterable<Candidate> candidates);

    void setPublicIds(List<SavedList> savedLists);

    /**
     * Update the info associated with the SavedList with the given id
     * - for example changing its name.
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the new list info
     * @return Updated saved list
     * @throws NoSuchObjectException if there is no saved list with this id.
     * @throws EntityExistsException if a list with the requested name already exists.
     */
    SavedList updateSavedList(long savedListId, UpdateSavedListInfoRequest request)
            throws NoSuchObjectException, EntityExistsException;


    /**
     * Adds a user who wants to share the given saved list (created by someone
     * else).
     * @param id id of Saved List being shared
     * @param request Contains the id of the user who is sharing the list
     * @return The updated saved list with a modified collection of users
     * who are sharing it.
     * @throws NoSuchObjectException if there is no saved list with this id
     * or the user is not found.
     */
    SavedList addSharedUser(long id, UpdateSharingRequest request)
            throws NoSuchObjectException;

    /**
     * Removes a user who was sharing the given saved list (created by someone
     * else).
     * @param id id of Saved List
     * @param request Contains the id of the user who is no longer interested
     *                in sharing the list
     * @return The updated saved list with a modified collection of users
     * who are sharing it.
     * @throws NoSuchObjectException if there is no saved list with this id
     * or the user is not found.
     */
    SavedList removeSharedUser(long id, UpdateSharingRequest request)
            throws NoSuchObjectException;

    /**
     * Updates the description of the given saved list.
     * @param savedListId Id of saved list
     * @param request Request containing the updated description
     * @throws NoSuchObjectException  if there is no saved list with this id
     */
    void updateDescription(long savedListId, UpdateCandidateSourceDescriptionRequest request)
        throws  NoSuchObjectException;

    /**
     * Updates the tbb short name used for redirecting to external links (google sheet).
     * @param request Request containing the updated short name and the saved list id which it belongs to.
     * @throws NoSuchObjectException  if there is no saved list with this id
     */
    SavedList updateTcShortName(UpdateShortNameRequest request)
        throws  NoSuchObjectException;

    /**
     * Returns all (undeleted) SavedLists which are associated with a job.
     * @return SavedLists - may be empty.
     */
    @NonNull
    List<SavedList> findListsAssociatedWithJobs();

    /**
     * Updates the tbb short name used for redirecting to external links (google sheet).
     * @param shortName Request containing the updated short name and the saved list id which it belongs to.
     * @throws NoSuchObjectException  if there is no saved list with this id
     */
    SavedList findByShortName(String shortName)
        throws  NoSuchObjectException;

    /**
     * Updates the fields that are displayed for each candidate in the given
     * saved list.
     * @param savedListId Id of saved list
     * @param request Request containing the field paths to be displayed.
     * @throws NoSuchObjectException  if there is no saved list with this id
     */
    void updateDisplayedFieldPaths(
            long savedListId, UpdateDisplayedFieldPathsRequest request)
            throws NoSuchObjectException;

    /**
     * Tags or untags the given candidate as pendingTermsAcceptance.
     * (This is done by adding or removing them from the associated SavedList).
     * <p>
     *     This means that we have informed the candidate that they need to accept new terms and
     *     that we are still waiting for them to do it.
     * </p>
     * @param candidate Candidate to be tagged/untagged
     * @param flag True if we are waiting for the candidate to accept terms.
     */
    void updatePendingTermsAcceptance(Candidate candidate, boolean flag);

    /**
     * Create a published external document from the data of candidates in the given list.
     * @param savedListId Id of saved list
     * @param request Request containing details of what is to be published
     * @return SavedList containing a link to the published doc
     * @throws GeneralSecurityException if there are security problems accessing document storage
     * @throws IOException if there are problems creating the document
     * @throws ReflectiveOperationException if the publish request contains unknown fields
     * @throws NoSuchObjectException  if there is no saved list with this id
     */
    SavedList publish(long savedListId, PublishListRequest request)
        throws GeneralSecurityException, IOException, NoSuchObjectException, ReflectiveOperationException;

    /**
     * Imports potential employer feedback from the currently published doc associated with a list.
     * <p/>
     * Does nothing if the list has not been published.
     * @param savedListId ID of published list
     * @return PublishedDocImportReport containing details of the import
     * @throws GeneralSecurityException if there are security problems accessing document storage
     * @throws IOException if there are problems creating the document
     * @throws NoSuchObjectException  if there is no saved list with this id or if published doc
     * is not found (maybe it has been manually deleted).
     */
    PublishedDocImportReport importEmployerFeedback(long savedListId)
        throws GeneralSecurityException, NoSuchObjectException, IOException;

    /**
     * Update audit fields and use repository to save the SavedList
     * @param savedList Entity to save
     * @return Saved entity
     */
    SavedList saveIt(SavedList savedList);

    /**
     * Fetches source list specified by attribute sourceListId in given request
     * @param request Request containing source list
     * @return SavedList if specified in request, null if non specified
     * @throws NoSuchObjectException if the list specified in the request does not exist
     */
    @Nullable
    SavedList fetchSourceList(UpdateSavedListContentsRequest request) throws NoSuchObjectException;

    /**
     * Updates the names of formally associated lists for the given Job, to reflect its new name.
     * Job renaming happens first - if for any reason that failed, this method has the virtue of
     * reproducing the old name.
     * @param job Job whose lists are to be renamed
     */
    void updateAssociatedListsNames(SalesforceJobOpp job);
}
