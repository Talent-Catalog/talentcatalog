package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.CandidateNoteRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.note.SearchCandidateNotesRequest;
import org.tbbtalent.server.request.note.UpdateCandidateNoteRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateNoteService;

import javax.transaction.Transactional;

@Service
public class CandidateNotesServiceImpl implements CandidateNoteService {

    private final CandidateRepository candidateRepository;
    private final CandidateNoteRepository candidateNoteRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateNotesServiceImpl(CandidateRepository candidateRepository, CandidateNoteRepository candidateNoteRepository,
                                     UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.candidateNoteRepository = candidateNoteRepository;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public Page<CandidateNote> searchCandidateNotes(SearchCandidateNotesRequest request) {
        return candidateNoteRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

    @Override
    public CandidateNote createCandidateNote(CreateCandidateNoteRequest request) {
        User user = userContext.getLoggedInUser();

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
        // Create a new note object to insert into the database
        CandidateNote candidateNote = new CandidateNote();
        candidateNote.setCandidate(candidate);
        candidateNote.setTitle(request.getTitle());
        candidateNote.setComment(request.getComment());
        candidateNote.setNoteType(user.getId().equals(candidate.getUser().getId()) ? NoteType.candidate : NoteType.admin);
        candidateNote.setAuditFields(user);
        // Save the candidateOccupation
        return candidateNoteRepository.save(candidateNote);
    }

    @Override
    public CandidateNote updateCandidateNote(long id, UpdateCandidateNoteRequest request) {
        CandidateNote candidateNote = this.candidateNoteRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));

        User user = userContext.getLoggedInUser();

        // Update education object to insert into the database
        candidateNote.setTitle(request.getTitle());
        candidateNote.setComment(request.getComment());
        candidateNote.setAuditFields(user);

        // Save the candidateOccupation
        return candidateNoteRepository.save(candidateNote);

    }

}
