/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.db.CandidateSkill;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {


    Page<CandidateSkill> findByCandidateId(Long candidateId, Pageable request);
}
