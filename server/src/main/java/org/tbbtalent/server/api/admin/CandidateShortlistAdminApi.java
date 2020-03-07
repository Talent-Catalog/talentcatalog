package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.CandidateShortlistItem;
import org.tbbtalent.server.request.shortlist.CreateCandidateShortlistRequest;
import org.tbbtalent.server.request.shortlist.UpdateCandidateShortlistRequest;
import org.tbbtalent.server.service.CandidateShortlistService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-shortlist")
public class CandidateShortlistAdminApi {

    private final CandidateShortlistService candidateShortlistService;

    @Autowired
    public CandidateShortlistAdminApi(CandidateShortlistService candidateShortlistService) {
        this.candidateShortlistService = candidateShortlistService;
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        CandidateShortlistItem shortlist = this.candidateShortlistService.getCandidateShortlistItem(id);
        return candidateShortlistDto().build(shortlist);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateCandidateShortlistRequest request) throws EntityExistsException {
        CandidateShortlistItem candidateShortlistItem = this.candidateShortlistService.createCandidateShortlist(request);
        return candidateShortlistDto().build(candidateShortlistItem);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateShortlistRequest request) {
        CandidateShortlistItem candidateShortlistItem = this.candidateShortlistService.updateCandidateShortlist(id, request);
        return candidateShortlistDto().build(candidateShortlistItem);
    }


    private DtoBuilder candidateShortlistDto() {
        return new DtoBuilder()
                .add("id")
                .add("shortlistStatus")
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
                ;
    }






}
