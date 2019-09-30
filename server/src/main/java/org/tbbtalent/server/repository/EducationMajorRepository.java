package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface EducationMajorRepository extends JpaRepository<EducationMajor, Long> {

    @Query(" select m from EducationMajor m "
            + " where m.status = :status")
    List<EducationMajor> findByStatus(@Param("status") Status status);
}
