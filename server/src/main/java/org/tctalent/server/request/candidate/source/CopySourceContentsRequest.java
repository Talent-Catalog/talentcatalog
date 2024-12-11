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

package org.tctalent.server.request.candidate.source;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.request.list.UpdateSavedListContentsRequest;

/**
 * Request to save the whole contents of a candidate source (either a saved list, or the selection
 * of a saved search) to a saved list (creating one if necessary).
 * <p/>
 * The actual contents copied is fully defined by the candidate source id:
 * <ul>
 *   <li>
 *     Saved List - the whole contents of the saved list
 *   </li>
 *   <li>
 *     Saved search - all currently selected candidates (ie the contents of the selection list)
 *   </li>
 * </ul>
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CopySourceContentsRequest extends UpdateSavedListContentsRequest {
  /**
   * Target list.
   * List id - 0 if new list requested
   */
  Long savedListId;

  /**
   * Only required if a new list is being created (savedListId == 0)
   */
  String newListName;

}
