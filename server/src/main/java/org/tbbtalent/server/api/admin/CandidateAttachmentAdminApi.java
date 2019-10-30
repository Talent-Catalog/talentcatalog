package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.request.attachment.UpdateCandidateAttachmentRequest;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.Map;

//import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
//import org.tbbtalent.server.request.note.UpdateCandidateAttachmentRequest;

@RestController()
@RequestMapping("/api/admin/candidate-attachment")
public class CandidateAttachmentAdminApi {

    private final CandidateAttachmentService candidateAttachmentService;

    @Autowired
    public CandidateAttachmentAdminApi(CandidateAttachmentService candidateAttachmentService) {
        this.candidateAttachmentService = candidateAttachmentService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateAttachmentsRequest request) {
        Page<CandidateAttachment> candidateAttachments = this.candidateAttachmentService.searchCandidateAttachments(request);
        return candidateAttachmentDto().buildPage(candidateAttachments);
    }

    @PostMapping()
    public Map<String, Object> createCandidateAttachment(@RequestBody CreateCandidateAttachmentRequest request) {
        CandidateAttachment candidateAttachment = candidateAttachmentService.createCandidateAttachment(request, true);
        return candidateAttachmentDto().build(candidateAttachment);
    }

    @PutMapping()
    public Map<String, Object> update(@RequestBody UpdateCandidateAttachmentRequest request) {
        CandidateAttachment candidateAttachment = this.candidateAttachmentService.updateCandidateAttachment(request);
        return candidateAttachmentDto().build(candidateAttachment);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateAttachment(@PathVariable("id") Long id) {
        candidateAttachmentService.deleteCandidateAttachment(id);
        return ResponseEntity.ok().build();
    }


    private DtoBuilder candidateAttachmentDto() {
        return new DtoBuilder()
                .add("id")
                .add("type")
                .add("name")
                .add("location")
                .add("fileType")
                .add("adminOnly")
                .add("migrated")
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
