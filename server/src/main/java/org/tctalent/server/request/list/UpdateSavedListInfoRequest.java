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
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;

/**
 * Request for modifying just the info associated with a SavedList (not the content)
 * - eg changing the name
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateSavedListInfoRequest extends AbstractUpdateCandidateSourceRequest {

  //TODO JC Should this go. Create jobs are done elsewhere. And you shouldnt be able to modify any old list to registered.
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
