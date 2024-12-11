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

package org.tctalent.server.service.db;

import org.springframework.data.domain.Page;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.request.note.CreateCandidateNoteRequest;
import org.tctalent.server.request.note.SearchCandidateNotesRequest;
import org.tctalent.server.request.note.UpdateCandidateNoteRequest;

public interface CandidateNoteService {

    Page<CandidateNote> searchCandidateNotes(SearchCandidateNotesRequest request);

    CandidateNote createCandidateNote(CreateCandidateNoteRequest request);

    CandidateNote updateCandidateNote(long id, UpdateCandidateNoteRequest request);


}
