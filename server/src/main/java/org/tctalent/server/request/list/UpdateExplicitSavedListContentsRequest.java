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

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request which includes explicit candidates used to update list contents
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateExplicitSavedListContentsRequest
    extends UpdateSavedListContentsRequest implements IHasSetOfCandidates {

  /**
   * Updating content
   */
  private Set<Long> candidateIds;

  public void addCandidateId(Long id) {
    if (candidateIds == null) {
      candidateIds = new HashSet<>();
    }
    candidateIds.add(id);
  }

}
