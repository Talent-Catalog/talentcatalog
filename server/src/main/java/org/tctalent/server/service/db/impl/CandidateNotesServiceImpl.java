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

package org.tctalent.server.service.db.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.repository.db.CandidateNoteRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.note.CreateCandidateNoteRequest;
import org.tctalent.server.request.note.SearchCandidateNotesRequest;
import org.tctalent.server.request.note.UpdateCandidateNoteRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateNoteService;

import jakarta.transaction.Transactional;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.User;

@Service
public class CandidateNotesServiceImpl implements CandidateNoteService {

    private final CandidateRepository candidateRepository;
    private final CandidateNoteRepository candidateNoteRepository;
    private final AuthService authService;

    @Autowired
    public CandidateNotesServiceImpl(CandidateRepository candidateRepository, CandidateNoteRepository candidateNoteRepository,
                                     AuthService authService) {
        this.candidateRepository = candidateRepository;
        this.candidateNoteRepository = candidateNoteRepository;
        this.authService = authService;
    }

    @Override
    @Transactional
    public Page<CandidateNote> searchCandidateNotes(SearchCandidateNotesRequest request) {
        return candidateNoteRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

    @Override
    public CandidateNote createCandidateNote(CreateCandidateNoteRequest request) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

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

        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        // Update education object to insert into the database
        candidateNote.setTitle(request.getTitle());
        candidateNote.setComment(request.getComment());
        candidateNote.setAuditFields(user);

        // Save the candidateOccupation
        return candidateNoteRepository.save(candidateNote);

    }

}
