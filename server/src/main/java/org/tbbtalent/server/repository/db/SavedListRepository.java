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
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.SavedList;

public interface SavedListRepository extends JpaRepository<SavedList, Long>, JpaSpecificationExecutor<SavedList> {

    @Query(" select distinct s from SavedList s "
            + " where lower(s.name) = lower(:name)"
            + " and s.createdBy.id = :userId"
    )
    Optional<SavedList> findByNameIgnoreCase(
            @Param("name") String name, @Param("userId")Long userId);

    @Query(" select distinct s from SavedList s left join fetch s.users"
            + " where s.id = :id" )
    Optional<SavedList> findByIdLoadUsers(@Param("id") long id);

    @Query(" select distinct s from SavedList s left join fetch s.candidateSavedLists"
            + " where s.id = :id" )
    Optional<SavedList> findByIdLoadCandidates(@Param("id") long id);

    @Query(" select distinct s from SavedList s " +
            " where s.createdBy.id = :userId " +
            " and s.savedSearch.id = :savedSearchId" )
    Optional<SavedList> findSelectionList(
            @Param("savedSearchId")long savedSearchId,
            @Param("userId")Long userId);

    @Query(" select distinct s from SavedList s " +
        " where s.registeredJob = true " +
        " and s.sfJobOpp.sfId = :sfId" )
    Optional<SavedList> findRegisteredJobList(@Param("sfId") String sfJoblink);

    @Query(" select distinct s from SavedList s "
            + " where lower(s.tbbShortName) = lower(:tbbShortName)")
    Optional<SavedList> findByShortNameIgnoreCase(@Param("tbbShortName") String tbbShortName);

    @Query(" select s from SavedList s where s.sfJobOpp is not null and s.status != 'deleted'")
    List<SavedList> findListsWithJobs();
}
