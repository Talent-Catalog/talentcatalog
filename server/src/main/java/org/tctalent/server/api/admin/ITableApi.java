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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.NotImplementedException;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * <h1>Base table API</h1>
 * Definition of standard web API controller methods for a normal table/entity.
 * <p/>
 * "id" refers to the id of a record in the base table.
 * <p/>
 * @see ITalentCatalogWebApi for overview of Talent Catalog Web APIs
 * <p/>
 * The SEARCH, CREATE and UPDATE refer to the standard SEARCH, CREATE and UPDATE Java request
 * classes which are processed by the API.
 * These classes are constructed from the JSON request body of the HTTP calls - using
 * the standard Spring framework support.
 * @author John Cameron
 */
public interface ITableApi<SEARCH, CREATE, UPDATE> extends ITalentCatalogWebApi {

    /**
     * Creates a new record from the data in the given request.
     * @param request Request containing details from which the record is created.
     * @return Created record
     * @throws EntityExistsException If an identical record (eg with the same
     * name) already exists
     */
    @PostMapping
    default @NotNull Map<String, Object> create(@Valid @RequestBody CREATE request)
            throws EntityExistsException {
        throw new NotImplementedException(this.getClass(), "create");
    }

    /**
     * Delete the record with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @DeleteMapping("{id}")
    default boolean delete(@PathVariable("id") long id)
            throws EntityReferencedException, InvalidRequestException {
        throw new NotImplementedException(this.getClass(), "delete");
    }

    /**
     * Get the record with the given id.
     * @param id ID of record to be returned
     * @return Requested record
     * @throws NoSuchObjectException if there is no such record with the given id
     */
    @GetMapping("{id}")
    default @NotNull Map<String, Object> get(@PathVariable("id") long id,
        @RequestParam(name = "dtoType", required = false, defaultValue = "EXTENDED") DtoType dtoType)
            throws NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "get");
    }

    /**
     * Get all records.
     * @return All records
     */
    @GetMapping()
    default @NotNull List<Map<String, Object>> list() {
        throw new NotImplementedException(this.getClass(), "list");
    }

    /**
     * Returns all records matching the given request.
     * <p/>
     * See also {@link #searchPaged}.
     * @param request Defines which records should be returned.
     *                (Any paging fields in the request are ignored.)
     * @return All matching records
     */
    @PostMapping("search")
    default @NotNull List<Map<String, Object>> search(
            @Valid @RequestBody SEARCH request) {
        throw new NotImplementedException(this.getClass(), "search");
    }

    /**
     * Returns a page of records matching the given request.
     * <p/>
     * See also {@link #search}.
     * @param request Defines which records should be returned, including
     *                paging details.
     * @return A paging record (see {@link DtoBuilder#buildPage}),
     * including a page of matching records in its content field.
     */
    @PostMapping("search-paged")
    default @NotNull Map<String, Object> searchPaged(
            @Valid @RequestBody SEARCH request) {
        throw new NotImplementedException(this.getClass(), "searchPaged");
    }

    /**
     * Update the record with the given id from the data in the given request.
     * @param id ID of record to be updated
     * @param request Request containing details from which the record is updated.
     *                Details which are not specified in the request (ie are null)
     *                cause no change to the record. Therefore, there is no way
     *                to set a field of the record to null.
     * @return Updated record
     * @throws EntityExistsException if the updated record would clash with an
     * existing record - eg with the same name.
     * @throws InvalidRequestException if not authorized to update this record.
     * @throws NoSuchObjectException if there is no such record with the given id
     */
    @PutMapping("{id}")
    default @NotNull Map<String, Object> update(
            @PathVariable("id") long id, @Valid @RequestBody UPDATE request)
            throws
            EntityExistsException, InvalidRequestException, NoSuchObjectException {
        throw new NotImplementedException(this.getClass(), "update");
    }

}
