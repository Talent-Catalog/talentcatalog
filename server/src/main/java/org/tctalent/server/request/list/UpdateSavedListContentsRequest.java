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

package org.tctalent.server.request.list;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;

/**
 * Request which will involved updating the list's contents.
 * The source of the candidates which will be used to update the contents is defined elsewhere,
 * either...
 * <ul>
 *   <li>
 *     In a subclassed {@link UpdateExplicitSavedListContentsRequest}
 *   </li>
 *   <li>
 *     The contents of another list
 *   </li>
 * </ul>
 * <p/>
 * Use Cases:
 * <ol>
 *   <li>
 *     Copy list (optional status update, update context)
 *   </li>
 *   <li>
 *     Copy selection from saved search - ie copy selection list  (optional status update, update context)
 *   </li>
 *   <li>
 *     Copy selection from list (optional status update, update context) -
 *     SavedListCandidateAdminApi.saveSelection
 *   </li>
 *   <li>
 *     Add/remove to list (no update of status or context) - SavedListCandidateAdminApi.merge
 *   </li>
 *   <li>
 *     Creat empty list  (no update of status or context)
 *   </li>
 *   <li>
 *     Update source info only
 *   </li>
 * </ol>
 *
 * This class has two superclasses
 * <ul>
 *   <li>
 *      {@link CopySourceContentsRequest} - for copying a whole list's contents (first two options).
 *      All that is is needed to define the contents is the list or search id.
 *   </li>
 *   <li>
 *      {@link UpdateExplicitSavedListContentsRequest} - for copying just the selected candidates
 *      from a source.
 *   </li>
 * </ul>
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateSavedListContentsRequest extends UpdateSavedListInfoRequest {

  /**
   * Add (really a merge because duplicates are not allowed), delete or
   * replace.
   */
  private ContentUpdateType updateType;

  /**
   * If present, indicates the list that the candidates came from
   * (allowing their context to be copied across).
   */
  @Nullable
  private Long sourceListId;

  /**
   * If present, the statuses of all updated candidates list should be set according to this.
   */
  @Nullable
  UpdateCandidateStatusInfo statusUpdateInfo;

}
