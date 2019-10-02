package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.model.SearchJoin;

public interface SearchJoinRepository extends JpaRepository<SearchJoin, Long> {

    @Modifying
    @Query(" delete from SearchJoin s "
            + " where s.savedSearch.id = :savedSearchId" )
    void deleteBySearchId(@Param("savedSearchId") Long savedSearchId);
}
