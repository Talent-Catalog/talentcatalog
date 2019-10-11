package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateNote;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CandidateNoteRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.UserRepository;
import org.tbbtalent.server.request.candidate.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.candidate.note.UpdateCandidateNoteRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateNoteService;

import java.util.List;

@Service
public class CandidateNoteServiceImpl implements CandidateNoteService {

    private final CandidateNoteRepository candidateNoteRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateNoteServiceImpl(CandidateNoteRepository candidateNoteRepository,
                                             CandidateRepository candidateRepository,
                                             UserRepository userRepository,
                                             UserContext userContext) {
        this.candidateNoteRepository = candidateNoteRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.userContext = userContext;
    }

    @Override
    public List<CandidateNote> list(long id) {
        return candidateNoteRepository.findByCandidateId(id);
    }

    @Override
    public CandidateNote createCandidateNote(CreateCandidateNoteRequest request) {
        Candidate candidate = this.candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));

        User user = this.userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NoSuchObjectException(User.class, request.getUserId()));

        // Create a new candidateOccupation object to insert into the database
        CandidateNote candidateNote = new CandidateNote();
        candidateNote.setCandidate(candidate);
        candidateNote.setSubject(request.getSubject());
        candidateNote.setComment(request.getComment());
        candidateNote.setCreatedDate(request.getCreatedDate());
        candidateNote.setUser(user);

        // Save the candidateOccupation
        return candidateNoteRepository.save(candidateNote);
    }

    @Override
    public CandidateNote updateCandidateNote(long id, UpdateCandidateNoteRequest request) {

        CandidateNote candidateNote = this.candidateNoteRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateNote.class, id));

        // Update education object to insert into the database
        candidateNote.setSubject(request.getSubject());
        candidateNote.setComment(request.getComment());
        candidateNote.setCreatedDate(request.getCreatedDate());

        // Save the candidateOccupation
        return candidateNoteRepository.save(candidateNote);

    }
}
