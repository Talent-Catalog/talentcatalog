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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.SavedSearch;

public interface SavedSearchRepository extends CacheEvictingRepository<SavedSearch, Long>, JpaSpecificationExecutor<SavedSearch> {

    /**
     * Deletes all {@link SavedSearch} entries associated with the specified job ID.
     *
     * @param jobId The ID of the job for which associated {@link SavedSearch} entries will be deleted.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM SavedSearch s WHERE s.sfJobOpp.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);

    @Query(" select distinct s from SavedSearch s "
            + " where lower(s.name) = lower(:name)"
            + " and s.createdBy.id = :userId"
    )
    SavedSearch findByNameIgnoreCase(
            @Param("name") String name, @Param("userId") long userId);

    @Query(" select distinct s from SavedSearch s "
            + " left join fetch s.searchJoins"
            + " where s.id = :id" )
    Optional<SavedSearch> findByIdLoadSearchJoins(@Param("id") long id);

    //Note that it is necessary to use the text version of the status, not
    //the enumerated value from the Status enum type which is what is stored
    //in the SavedSearch entity. ie 'deleted' rather than Status.deleted.
    //Theoretically it should work to use Status.deleted.
    //(see, for example, https://stackoverflow.com/questions/8217144/problems-with-making-a-query-when-using-enum-in-entity)
    //But it doesn't in this code for some reason.
    //Fortunately using the text value (which is what is actually in the
    //database) does work.
    // - JC, 7 Jul 2020
    @Query(" select distinct s from SavedSearch s "
            + " left join fetch s.users"
            + " where s.id = :id"
            + " and s.status <> 'deleted'"
    )
    Optional<SavedSearch> findByIdLoadUsers(@Param("id") long id);

    @Query(" select distinct s from SavedSearch s "
            + " left join fetch s.createdBy"
            + " where s.id = :id" )
    Optional<SavedSearch> findByIdLoadAudit(@Param("id") long id);

    @Query(" select distinct s from SavedSearch s "
            + " where s.watcherIds is not null "
            + " and s.status <> 'deleted'"
    )
    Set<SavedSearch> findByWatcherIdsIsNotNull();

    @Query(value=" select * from saved_search s "
            + " where cast(:userId as text) in " +
            " (select * from regexp_split_to_table(s.watcher_ids, ','))" +
            " and s.status <> 'deleted'",
            nativeQuery = true )
    Set<SavedSearch> findUserWatchedSearches(@Param("userId") long userId);


    @Query(" select distinct s from SavedSearch s " +
            " where s.createdBy.id = :userId " +
            " and s.defaultSearch = true" )
    Optional<SavedSearch> findDefaultSavedSearch(@Param("userId")Long userId);

    @Query(" select s from SavedSearch s where s.id in (:ids) order by s.name")
    List<SavedSearch> findByIds(@Param("ids") Iterable<Long> ids);

}
