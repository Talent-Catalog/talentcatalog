package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
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
//        return candidateAttachmentDto().build(candidateAttachment);
//
//    @PutMapping("{id}")
//    public Map<String, Object> update(@PathVariable("id") long id,
//                                      @RequestBody UpdateCandidateAttachmentRequest request) {
//        CandidateAttachment candidateAttachment = this.candidateAttachmentService.updateCandidateAttachment(id, request);
//        return candidateAttachmentDto().build(candidateAttachment);
//    }


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
