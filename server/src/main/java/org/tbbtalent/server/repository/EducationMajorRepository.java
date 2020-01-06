package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface EducationMajorRepository extends JpaRepository<EducationMajor, Long>, JpaSpecificationExecutor<EducationMajor> {

    @Query(" select m from EducationMajor m "
            + " where m.status = :status")
    List<EducationMajor> findByStatus(@Param("status") Status status);

    @Query(" select distinct m from EducationMajor m "
            + " where lower(m.name) = lower(:name)"
            + " and m.status != 'deleted'" )
    EducationMajor findByNameIgnoreCase(@Param("name") String name);

    @Query(" select m.name from EducationMajor m "
            + " where m.id in (:ids) order by m.name asc" )
    List<String> getNamesForIds(@Param("ids") List<Long> ids);
}
