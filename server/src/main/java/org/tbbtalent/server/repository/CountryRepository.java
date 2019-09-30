package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {


    @Query(" select c from Country c "
            + " where c.status = :status")
    List<Country> findByStatus(@Param("status") Status status);
}
