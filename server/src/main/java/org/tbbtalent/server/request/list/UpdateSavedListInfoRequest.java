/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request.list;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;

/**
 * Request for modifying just the info associated with a SavedList (not the content)
 * - eg changing the name
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateSavedListInfoRequest extends AbstractUpdateCandidateSourceRequest {

  /**
   * @see SavedList
   */
  @Nullable
  private Boolean registeredJob;

  /**
   * Populates the given SavedList from this request
   * @param savedList List to be populated
   */
  public void populateFromRequest(SavedList savedList) {
    savedList.setRegisteredJob(registeredJob);

    //Registered jobs are always global
    if (registeredJob != null && registeredJob) {
      savedList.setGlobal(true);
    }

    super.populateFromRequest(savedList);
  }
}
