package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface IndustryRepository extends JpaRepository<Industry, Long> {

    @Query(" select i from Industry i "
            + " where i.status = :status")
    List<Industry> findByStatus(@Param("status") Status status);
}
