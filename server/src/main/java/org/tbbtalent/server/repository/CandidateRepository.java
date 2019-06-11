package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Page<Candidate> findByFirstName(String firstName, Pageable pageable);

    Page<Candidate> findByFirstNameIgnoreCase(String firstName, Pageable pageable);

    Page<Candidate> findByFirstNameOrLastName(String firstName, String lastName, Pageable pageable);
}
