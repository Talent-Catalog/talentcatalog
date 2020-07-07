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
            + " left join fetch s.searchJoins"
            + " where s.watcherIds is not null " 
            + " and s.status <> 'deleted'"
    )
    Set<SavedSearch> findByWatcherIdsIsNotNullLoadSearchJoins();

    @Query(value=" select * from saved_search s "
            + " where cast(:userId as text) in " +
            " (select * from regexp_split_to_table(s.watcher_ids, ','))" +
            " and s.status <> 'deleted'", 
            nativeQuery = true )
    Set<SavedSearch> findUserWatchedSearches(@Param("userId") long userId);
}
