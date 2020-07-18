/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.SearchJoin;

public interface SearchJoinRepository extends JpaRepository<SearchJoin, Long> {

    @Modifying
    @Query(" delete from SearchJoin s "
            + " where s.savedSearch.id = :savedSearchId" )
    void deleteBySearchId(@Param("savedSearchId") Long savedSearchId);
}
