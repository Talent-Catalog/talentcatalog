/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

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
}                       
