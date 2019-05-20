package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}
