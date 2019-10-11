package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.model.CandidateNote;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.note.SearchCandidateNotesRequest;

import java.util.List;

public interface CandidateNoteService {

    Page<CandidateNote> searchCandidateNotes(SearchCandidateNotesRequest request);

    CandidateNote createCandidateNote(CreateCandidateNoteRequest request);



}
