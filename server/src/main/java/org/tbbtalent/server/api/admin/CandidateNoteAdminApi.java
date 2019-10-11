package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.CandidateNote;
import org.tbbtalent.server.service.CandidateNoteService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-note")
public class CandidateNoteAdminApi {

    private final CandidateNoteService candidateNoteService;

    @Autowired
    public CandidateNoteAdminApi(CandidateNoteService candidateNoteService) {
        this.candidateNoteService = candidateNoteService;
    }


    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateNote> candidateNotes = this.candidateNoteService.list(id);
        return candidateNoteDto().buildList(candidateNotes);
    }

//    @PostMapping("{id}")
//    public Map<String, Object> create(@PathVariable("id") long candidateId,
//                                      @RequestBody CreateCandidateEducationRequest request) throws UsernameTakenException {
//        CandidateEducation candidateEducation = this.candidateEducationService.createCandidateEducationAdmin(candidateId, request);
//        return candidateEducationDto().build(candidateEducation);
//    }
//
//    @PutMapping("{id}")
//    public Map<String, Object> update(@PathVariable("id") long id,
//                                      @RequestBody UpdateCandidateEducationRequest request) {
//        CandidateEducation candidateEducation = this.candidateEducationService.updateCandidateEducationAdmin(id, request);
//        return candidateEducationDto().build(candidateEducation);
//    }


    private DtoBuilder candidateNoteDto() {
        return new DtoBuilder()
                .add("id")
                .add("subject")
                .add("comment")
                .add("user", userDto())
                .add("createdDate")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("firstName")
                .add("lastName")
                .add("email")
                .add("status")
                ;
    }

}
