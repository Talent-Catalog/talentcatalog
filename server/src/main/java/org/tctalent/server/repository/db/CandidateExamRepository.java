/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.Exam;

import java.util.Optional;

public interface CandidateExamRepository
        extends JpaRepository<CandidateExam, Long> {

    @Query(" select e from CandidateExam e "
            + " left join e.candidate c "
            + " where e.id = :id")
    Optional<CandidateExam> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select distinct e from CandidateExam e "
            + " left join e.candidate c "
            + " where c.id = :candidateId"
            + " and e.exam = :examType"
            + " and e.id <> :id")
    Optional<CandidateExam> findDuplicateByExamType(@Param("examType") Exam examType,
                                           @Param("candidateId") Long candidateId,
                                                    @Param("id") Long id);

    @Query("select e from CandidateExam e where e.candidate.id = :candidateId")
    List<CandidateExam> findByCandidateId(@Param("candidateId") Long candidateId);

    @Query("select e from CandidateExam e where e.candidate.id = :candidateId and e.exam = :exam")
    CandidateExam findByCandidateIdAndExam(@Param("candidateId") Long candidateId, @Param("exam") Exam exam);
}
