package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateNote;
import org.tbbtalent.server.request.candidate.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.candidate.note.UpdateCandidateNoteRequest;

import java.util.List;

public interface CandidateNoteService {

    CandidateNote createCandidateNote(CreateCandidateNoteRequest request);

    CandidateNote updateCandidateNote(long id, UpdateCandidateNoteRequest request);

    List<CandidateNote> list(long id);

}
