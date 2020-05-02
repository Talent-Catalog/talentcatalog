package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.SavedSearch;

public interface SavedListRepository extends JpaRepository<SavedList, Long>, JpaSpecificationExecutor<SavedSearch> {
    //todo

    @Query(" select distinct s from SavedList s "
            + " where lower(s.name) = lower(:name)" )
    SavedList findByNameIgnoreCase(@Param("name") String name);
    
}
