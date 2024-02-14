/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateVisaJobCheck;

public interface CandidateVisaJobRepository
        extends JpaRepository<CandidateVisaJobCheck, Long> {
  // TODO decide if we need this (it's for accessing from createOrUpdateCandidateOpportunity if we want to update relocation info at certain stages).
  @Query(" select cvjc from CandidateVisaJobCheck cvjc "
          + " inner join cvjc.candidateVisaCheck cvc "
          + " where cvc.candidate.id = :candidateId "
          + " and cvjc.jobOpp.id = :jobOppId")
  Optional<CandidateVisaJobCheck> findByCandidateIdAndJobOppId(
      @Param("candidateId") long candidateId,
      @Param("jobOppId") long jobOppId);
}
