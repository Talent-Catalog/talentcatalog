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

package org.tctalent.server.api.admin;

import java.util.Map;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.request.note.CreateCandidateNoteRequest;
import org.tctalent.server.request.note.SearchCandidateNotesRequest;
import org.tctalent.server.request.note.UpdateCandidateNoteRequest;
import org.tctalent.server.service.db.CandidateNoteService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-note")
@RequiredArgsConstructor
public class CandidateNoteAdminApi {

    private final CandidateNoteService candidateNoteService;

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateNotesRequest request) {
        Page<CandidateNote> candidateNotes = candidateNoteService.searchCandidateNotes(request);
        return candidateNoteDto().buildPage(candidateNotes);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateCandidateNoteRequest request) throws EntityExistsException {
        CandidateNote candidateNote = candidateNoteService.createCandidateNote(request);
        return candidateNoteDto().build(candidateNote);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateNoteRequest request) {
        CandidateNote candidateNote = candidateNoteService.updateCandidateNote(id, request);
        return candidateNoteDto().build(candidateNote);
    }

    private DtoBuilder candidateNoteDto() {
        return new DtoBuilder()
                .add("id")
                .add("noteType")
                .add("title")
                .add("comment")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                .add("partner", partnerDto())
                ;
    }

    private DtoBuilder partnerDto() {
        return new DtoBuilder()
                .add("abbreviation")
                ;
    }

}
