package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface NationalityRepository extends JpaRepository<Nationality, Long>, JpaSpecificationExecutor<Nationality> {

    @Query(" select n from Nationality n "
            + " where n.status = :status")
    List<Nationality> findByStatus(@Param("status") Status status);

    @Query(" select distinct n from Nationality n "
            + " where lower(n.name) = lower(:name) ")
    Nationality findByNameIgnoreCase(@Param("name") String name);
}
