package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Query(" select c from Candidate c "
            + " where c.email = :username "
            + " or c.phone = :username "
            + " or c.whatsapp = :username ")
    Candidate findByUsernameIgnoreCase(@Param("username") String username);

}
