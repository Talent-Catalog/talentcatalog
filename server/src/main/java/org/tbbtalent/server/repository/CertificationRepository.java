package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Certification;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    @Query(" select f from Certification f "
            + " left join f.candidate c "
            + " where f.id = :id")
    Optional<Certification> findByIdLoadCandidate(@Param("id") Long id);
}
