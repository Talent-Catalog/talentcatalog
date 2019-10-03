package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface OccupationRepository extends JpaRepository<Occupation, Long>, JpaSpecificationExecutor<Occupation> {

    @Query(" select o from Occupation o "
            + " where o.status = :status")
    List<Occupation> findByStatus(@Param("status") Status status);

    @Query(" select distinct o from Occupation o "
            + " where lower(o.name) = lower(:name)"
            + " and o.status != 'deleted'" )
    Occupation findByNameIgnoreCase(@Param("name") String name);

}
