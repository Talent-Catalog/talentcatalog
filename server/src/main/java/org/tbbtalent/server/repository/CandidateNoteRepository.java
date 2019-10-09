package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.CandidateNote;
import org.tbbtalent.server.request.note.SearchCandidateNotesRequest;

public interface CandidateNoteRepository extends JpaRepository<CandidateNote, Long> {


    Page<CandidateNote> findByCandidateId(Long candidateId, PageRequest request);
}
