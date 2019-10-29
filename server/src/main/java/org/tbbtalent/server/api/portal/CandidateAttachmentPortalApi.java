package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.request.SearchRequest;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.Map;

@RestController()
@RequestMapping("/api/portal/candidate-attachment")
public class CandidateAttachmentPortalApi {

    private final CandidateAttachmentService candidateAttachmentService;

    @Autowired
    public CandidateAttachmentPortalApi(CandidateAttachmentService candidateAttachmentService) {
        this.candidateAttachmentService = candidateAttachmentService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchRequest request) {
        Page<CandidateAttachment> candidateAttachments = this.candidateAttachmentService.searchCandidateAttachmentsForLoggedInCandidate(request);
        return candidateAttachmentDto().buildPage(candidateAttachments);
    }

//    @PostMapping()
//    public Map<String, Object> createCandidateAttachment(@RequestBody CreateCandidateAttachmentRequest request) {
//        CandidateAttachment candidateAttachment = candidateAttachmentService.createCandidateAttachment(request);
//        return candidateAttachmentDto().build(candidateAttachment);
//    }
//
//    @DeleteMapping("{id}")
//    public ResponseEntity deleteCandidateAttachment(@PathVariable("id") Long id) {
//        candidateAttachmentService.deleteCandidateAttachment(id);
//        return ResponseEntity.ok().build();
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
