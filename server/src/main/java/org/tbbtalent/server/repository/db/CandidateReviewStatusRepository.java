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

package org.tbbtalent.server.repository.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateReviewStatusItem;
import org.tbbtalent.server.model.db.ReviewStatus;

public interface CandidateReviewStatusRepository extends JpaRepository<CandidateReviewStatusItem, Long> {

  @Query(" select review.candidate from CandidateReviewStatusItem review "
      + " where review.savedSearch.id = :savedSearchId and not review.reviewStatus in (:statuses) ")
  List<Candidate> findCandidatesExcludedFromSearch(
      @Param("savedSearchId") Long savedSearchId, @Param("statuses") List<ReviewStatus> statuses);

}
