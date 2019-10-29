package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.request.SearchRequest;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/candidate-attachment")
public class CandidateAttachmentPortalApi {

    private final CandidateAttachmentService candidateAttachmentService;

    @Autowired
    public CandidateAttachmentPortalApi(CandidateAttachmentService candidateAttachmentService) {
        this.candidateAttachmentService = candidateAttachmentService;
    }

    @GetMapping()
    public List<Map<String, Object>> list() {
        List<CandidateAttachment> candidateAttachments = this.candidateAttachmentService.listCandidateAttachmentsForLoggedInCandidate();
        return candidateAttachmentDto().buildList(candidateAttachments);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchRequest request) {
        Page<CandidateAttachment> candidateAttachments = this.candidateAttachmentService.searchCandidateAttachmentsForLoggedInCandidate(request);
        return candidateAttachmentDto().buildPage(candidateAttachments);
    }

    @PostMapping()
    public Map<String, Object> createCandidateAttachment(@RequestBody CreateCandidateAttachmentRequest request) {
        CandidateAttachment candidateAttachment = candidateAttachmentService.createCandidateAttachment(request, false);
        return candidateAttachmentDto().build(candidateAttachment);
    }
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
