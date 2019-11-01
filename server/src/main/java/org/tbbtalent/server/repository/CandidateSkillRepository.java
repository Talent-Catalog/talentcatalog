package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.CandidateNote;
import org.tbbtalent.server.model.CandidateSkill;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {


    Page<CandidateSkill> findByCandidateId(Long candidateId, Pageable request);
}
