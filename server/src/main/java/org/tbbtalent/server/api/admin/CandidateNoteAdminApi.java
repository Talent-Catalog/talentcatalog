package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.CandidateNote;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.note.SearchCandidateNotesRequest;
import org.tbbtalent.server.request.note.UpdateCandidateNoteRequest;
import org.tbbtalent.server.service.CandidateNoteService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-note")
public class CandidateNoteAdminApi {

    private final CandidateNoteService candidateNoteService;

    @Autowired
    public CandidateNoteAdminApi(CandidateNoteService candidateNoteService) {
        this.candidateNoteService = candidateNoteService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateNotesRequest request) {
        Page<CandidateNote> candidateNotes = this.candidateNoteService.searchCandidateNotes(request);
        return candidateNoteDto().buildPage(candidateNotes);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateCandidateNoteRequest request) throws EntityExistsException {
        CandidateNote candidateNote = this.candidateNoteService.createCandidateNote(request);
        return candidateNoteDto().build(candidateNote);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateNoteRequest request) {
        CandidateNote candidateNote = this.candidateNoteService.updateCandidateNote(id, request);
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
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }


}
