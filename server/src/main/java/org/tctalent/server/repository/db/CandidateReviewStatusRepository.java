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

package org.tctalent.server.repository.db;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.ReviewStatus;

public interface CandidateReviewStatusRepository extends JpaRepository<CandidateReviewStatusItem, Long> {

  @Query(" select review.candidate from CandidateReviewStatusItem review "
      + " where review.savedSearch.id = :savedSearchId and review.reviewStatus in (:statuses) ")
  Set<Candidate> findReviewedCandidatesForSearch(
      @Param("savedSearchId") Long savedSearchId, @Param("statuses") List<ReviewStatus> statuses);

}
