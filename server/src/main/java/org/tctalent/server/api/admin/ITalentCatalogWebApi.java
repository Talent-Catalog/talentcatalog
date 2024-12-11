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

import org.tctalent.server.util.dto.DtoBuilder;

/**
 * <h1>Overview of Talent Catalog Web APIs</h1>
 * In the Web APIs (admin or portal), there is basically one api per DB table.
 * For example  CandidateAdminApi or CandidatePortalApi for the Candidate table.
 * <p/>
 * There are three types of table which have slightly different API's:
 * <ul>
 *     <li>Base table, see {@link ITableApi} - eg candidate - </li>
 *     <li>Joined table, see {@link IJoinedTableApi} - eg candidate_note</li>
 *     <li>Many to many association table, see {@link IManyToManyApi} -
 *     eg candidate_saved_list</li>
 * </ul>
 * <p/>
 * <h2>Returned Values</h2>
 * <p/>
 * Each returned table record is in the form of a Map of name value
 * pairs Map(String, Object). In the browser code this ends up as a TypeScript
 * object of keys and values.
 * <p/>
 * Multiple records are represented as a list of the above: List(Map(String,Object))
 * In the browser code this ends up as an array of Typescript objects.
 * <p/>
 * Un paged multiple records are simply returned as List(Map(String,Object))
 * <p/>
 * Paged returns of multiple records are returned as a single Map(String,Object)
 * which consists of a standard set of name value pairs, one of which is
 * "content" which is a List(Map(String,Object)) representing the content of the
 * page. See {@link DtoBuilder#buildPage} for the details of a paging record.
 * <p/>
 * Boolean - Returns false if no record with the requested id exists,
 * otherwise returns true
 * <p/>
 * <h2>Implementing Methods</h2>
 * To implement a method in a controller, just copy the method signature but
 * omitting the annotations - they will be inherited from the interface.
 * <p/>
 * Note that you only have to implement the methods you need.
 * The default implementations will throw an exception if they are
 * mistakenly called.
 *
 * @author John Cameron
 */
public interface ITalentCatalogWebApi {
}
