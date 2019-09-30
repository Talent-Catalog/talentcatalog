package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface OccupationRepository extends JpaRepository<Occupation, Long> {

    @Query(" select o from Occupation o "
            + " where o.status = :status")
    List<Occupation> findByStatus(@Param("status") Status status);

}
