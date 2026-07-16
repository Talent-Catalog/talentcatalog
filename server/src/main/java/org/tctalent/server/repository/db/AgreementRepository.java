/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Agreement;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.model.db.TermsType;

/**
 * Repository for managing Agreement entities.
 *
 * @author sadatmalik
 */
public interface AgreementRepository extends JpaRepository<Agreement, Long> {

    List<Agreement> findByCandidateIdOrderByStartDesc(Long candidateId);

    //MODEL: JOIN FETCH in a @Query eagerly loads a LAZY association in a single SQL JOIN,
    // avoiding N+1 queries when the association is needed in the same transaction.
    // Use when a specific query needs an association that is LAZY by default on the entity.
    @Query("SELECT a FROM Agreement a JOIN FETCH a.counterparty "
        + "WHERE a.candidate.id = :candidateId ORDER BY a.start DESC")
    List<Agreement> findWithCounterpartyByCandidateIdOrderByStartDesc(@Param("candidateId") Long candidateId);

    List<Agreement> findByCandidateIdAndCounterpartyTypeOrderByStartDesc(
        Long candidateId, CounterpartyType counterpartyType);

    Optional<Agreement> findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
        Long candidateId, Long counterpartyId, TermsType termsType);
}
