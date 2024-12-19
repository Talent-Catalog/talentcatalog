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
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.SavedList;

public interface SavedListRepository extends CacheEvictingRepository<SavedList, Long>, JpaSpecificationExecutor<SavedList> {

    /**
     * Retrieves a list of {@link SavedList} entries associated with the specified job IDs.
     * @param jobIds The IDs of the jobs for which associated {@link SavedList} entries will be retrieved.
     * @return A list of {@link SavedList} entries associated with the specified job IDs.
     */
    @Query("SELECT s FROM SavedList s WHERE s.sfJobOpp.id IN :jobIds")
    List<SavedList> findByJobIds(@Param("jobIds") Long jobIds);
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
            + " where lower(s.tcShortName) = lower(:tcShortName)")
    Optional<SavedList> findByShortNameIgnoreCase(@Param("tcShortName") String tcShortName);

    @Query(" select s from SavedList s where s.sfJobOpp is not null and s.status != 'deleted'")
    List<SavedList> findListsWithJobs();

    @Query(" select s from SavedList s where s.id in (:ids) order by s.name")
    List<SavedList> findByIds(@Param("ids") Iterable<Long> ids);

    @Query(value = "select csl.candidate_id from candidate_saved_list csl "
        + "where csl.saved_list_id in (:listIds)", nativeQuery = true)
    Set<Long> findUnionOfCandidates(@Param("listIds") List<Long> listIds);
}
