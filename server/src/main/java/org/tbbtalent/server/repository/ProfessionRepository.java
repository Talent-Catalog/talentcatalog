package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Profession;

import java.util.Optional;

public interface ProfessionRepository extends JpaRepository<Profession, Long> {

    @Query(" select p from Profession p "
            + " left join p.candidate c "
            + " where p.id = :id")
    Optional<Profession> findByIdLoadCandidate(@Param("id") Long id);
}
