package org.tbbtalent.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.SavedSearch;

public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long>, JpaSpecificationExecutor<SavedSearch> {

    @Query(" select distinct s from SavedSearch s "
            + " where lower(s.name) = lower(:name)" )
    SavedSearch findByNameIgnoreCase(@Param("name") String name);

    @Query(" select distinct s from SavedSearch s "
            + " left join fetch s.searchJoins"
            + " where s.id = :id" )
    Optional<SavedSearch> findByIdLoadSearchJoins(@Param("id") long id);

    @Query(" select distinct s from SavedSearch s "
            + " left join fetch s.users"
            + " where s.id = :id" )
    Optional<SavedSearch> findByIdLoadUsers(@Param("id") long id);

    @Query(" select distinct s from SavedSearch s "
            + " left join fetch s.searchJoins"
            + " where s.watcherIds is not null " )
    List<SavedSearch> findByWatcherIdsIsNotNullLoadSearchJoins();
}
