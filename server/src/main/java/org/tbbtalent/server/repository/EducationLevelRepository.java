package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface EducationLevelRepository extends JpaRepository<EducationLevel, Long>, JpaSpecificationExecutor<EducationLevel> {

    @Query(" select l from EducationLevel l "
            + " where l.status = :status order by l.level")
    List<EducationLevel> findByStatus(@Param("status") Status status);

    @Query(" select distinct l from EducationLevel l "
            + " where lower(l.name) = lower(:name)"
            + " and l.status != 'deleted' order by l.level" )
    EducationLevel findByNameIgnoreCase(@Param("name") String name);


    @Query(" select distinct l from EducationLevel l "
            + " where l.level = :level"
            + " and l.status != 'deleted'" )
    EducationLevel findByLevelIgnoreCase(@Param("level") int level);
}
