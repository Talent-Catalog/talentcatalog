package org.tbbtalent.server.repository;

import java.util.Optional;
import java.util.Set;

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
            + " left join fetch s.createdBy"
            + " where s.id = :id" )
    Optional<SavedSearch> findByIdLoadAudit(@Param("id") long id);

    @Query(" select distinct s from SavedSearch s "
            + " left join fetch s.searchJoins"
            + " where s.watcherIds is not null " )
    Set<SavedSearch> findByWatcherIdsIsNotNullLoadSearchJoins();

    @Query(value=" select * from saved_search s "
            + " where cast(:userId as text) in " +
            " (select * from regexp_split_to_table(s.watcher_ids, ','))", 
            nativeQuery = true )
    Set<SavedSearch> findUserWatchedSearches(@Param("userId") long userId);
}
