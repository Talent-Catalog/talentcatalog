package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface NationalityRepository extends JpaRepository<Nationality, Long> {

    @Query(" select n from Nationality n "
            + " where n.status = :status")
    List<Nationality> findByStatus(@Param("status") Status status);
}
