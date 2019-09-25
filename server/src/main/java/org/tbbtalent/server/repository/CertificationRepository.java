package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.CandidateCertification;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<CandidateCertification, Long> {

    @Query(" select f from CandidateCertification f "
            + " left join f.candidate c "
            + " where f.id = :id")
    Optional<CandidateCertification> findByIdLoadCandidate(@Param("id") Long id);
}
