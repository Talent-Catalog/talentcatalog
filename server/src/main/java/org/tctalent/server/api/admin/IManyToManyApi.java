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

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.NotImplementedException;

/**
 * <h1>Many to Many Association API</h1>
 * Definition of standard web API controller methods for a Many to Many
 * table.
 * <p/>
 * A many to many table just contains relationships between two other tables,
 * each of which will have its own Web API for creating, updating and deleting
 * records.
 * The API for this kind of table will just do searches and add and remove
 * relationships.
 * <p/>
 * One side of the many to many relationship can be considered the "master" -
 * and that will be the first named in the API name.
 * For example, related to the many to many relationship defined by the joining
 * table candidate_saved_list, there could be two APIs:
 * CandidateSavedListAPI, where the Candidate would be considered as the
 * "master", and
 * SavedListCandidateAPI, where the SavedList would be considered as the master.
 * <p/>
 * "masterId" refers to the id of a record in the master table.
 * <p/>
 * "slaveId" refers to the other table in the many to many relationship.
 * <p/>
 * @see ITalentCatalogWebApi for overview of Talent Catalog Web APIs
 * <p/>
 * @author John Cameron
 */
public interface IManyToManyApi<SEARCH, CONTENT> extends ITalentCatalogWebApi {

    /**
     * Returns all Slave records associated with master.
     * <p/>
     * @param masterId ID of master record whose slave records we want
     * @return All slave records associated with master
     * @throws NoSuchObjectException if masterId is unknown
     */
    @GetMapping("{id}/list")
    default @NotNull List<Map<String, Object>> list(
            @PathVariable("id") long masterId)
            throws NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "list");
    }

    /**
     * Associate a number of slaves with the given master. They are added
     * (merged) with any existing slaves (merged in the sense that if a slave
     * is already present it remains - there are no duplicates).
     * The request body will contain a set (no duplicates) of "slave ids" to be
     * added to the existing set of slave ids associated with the given
     * master id.
     * <p/>
     * See also {@link #replace}
     * @param masterId ID of master record associated with slave records
     * @param request The request contains a set (no duplicates) of "slave ids"
     *                to be added to the existing set of slave ids.
     * @throws NoSuchObjectException if any ids are unknown
     */
    @PutMapping("{id}/merge")
    default void merge(
            @PathVariable("id") long masterId, @Valid @RequestBody CONTENT request)
            throws NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "merge");
    }

    /**
     * Remove slaves from the given master.
     * The request body will contain a set (no duplicates) of "slave ids"
     * to be removed from the current set of slaves associated with the given
     * master id.
     * @param masterId ID of master record associated with slave records
     * @param request The request contains a set (no duplicates) of "slave ids"
     *                to be removed.
     * @throws NoSuchObjectException if any ids are unknown
     */
    @PutMapping("{id}/remove")
    default void remove(
            @PathVariable("id") long masterId, @Valid @RequestBody CONTENT request)
            throws NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "remove");
    }

    /**
     * Associate a number of slaves with the given master. They replace
     * any existing slaves.
     * The request body will contain a set (no duplicates) of "slave ids" which
     * will become the new set of slave ids associated with the given
     * master id.
     * <p/>
     * Note that providing an empty or null set in the request, is valid and
     * clears out all associations.
     * <p/>
     * See also {@link #merge}
     * @param masterId ID of master record associated with slave records
     * @param request The request contains a set (no duplicates) of "slave ids"
     *                become new set of slave ids.
     * @throws NoSuchObjectException if any ids are unknown
     */
    @PutMapping("{id}/replace")
    default void replace(
            @PathVariable("id") long masterId, @Valid @RequestBody CONTENT request)
            throws NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "replace");
    }

    /**
     * Returns all Slave records matching the request.
     * <p/>
     * @param masterId ID of master record whose slave records we want
     * @param request Defines which slave records should be returned.
     * @return All matching slave records associated with the given master
     * @throws NoSuchObjectException if masterId is unknown
     */
    @PostMapping("{id}/search")
    default @NotNull List<Map<String, Object>> search(
            @PathVariable("id") long masterId, @Valid @RequestBody SEARCH request)
            throws NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "search");
    }

    /**
     * Returns a page of Slave records matching the request.
     * <p/>
     * @param masterId ID of master record whose slave records we want
     * @param request Defines which slave records should be returned.
     * @return A page of slave records associated with the given master
     * @throws NoSuchObjectException if masterId is unknown
     */
    @PostMapping("{id}/search-paged")
    default @NotNull Map<String, Object> searchPaged(
            @PathVariable("id") long masterId, @Valid @RequestBody SEARCH request)
            throws NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "searchPaged");
    }

}
