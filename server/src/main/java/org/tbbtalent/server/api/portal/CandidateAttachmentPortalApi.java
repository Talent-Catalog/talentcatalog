package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.request.PagedSearchRequest;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.service.db.CandidateAttachmentService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.io.IOException;
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
    public Map<String, Object> search(@RequestBody PagedSearchRequest request) {
        Page<CandidateAttachment> candidateAttachments = this.candidateAttachmentService.searchCandidateAttachmentsForLoggedInCandidate(request);
        return candidateAttachmentDto().buildPage(candidateAttachments);
    }

    @PostMapping()
    public Map<String, Object> createCandidateAttachment(@RequestBody CreateCandidateAttachmentRequest request) {
        CandidateAttachment candidateAttachment = candidateAttachmentService.createCandidateAttachment(request);
        return candidateAttachmentDto().build(candidateAttachment);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> deleteCandidateAttachment(@PathVariable("id") Long id) {
        candidateAttachmentService.deleteCandidateAttachment(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Upload an attachment associated with the currently logged in candidate and
     * creates a CandidateAttachment record on the database.
     * <p/>
     * Processes uploaded file and then uploads it again to Google Drive.
     * This replaces the old {@link #createCandidateAttachment}
     * @param cv True if the attachment is a CV (in which case its text is 
     *           extracted for keywords).
     * @param file Attachment file           
     * @return Candidate attachment
     */
    @PostMapping("upload")
    public Map<String, Object> uploadAttachment(@RequestParam("cv") Boolean cv,
            @RequestParam("file") MultipartFile file )
            throws IOException {
        CandidateAttachment candidateAttachment =
                candidateAttachmentService.uploadAttachment(cv, file);
        return candidateAttachmentDto().build(candidateAttachment);
    }
    

    private DtoBuilder candidateAttachmentDto() {
        return new DtoBuilder()
                .add("id")
                .add("type")
                .add("name")
                .add("location")
                .add("fileType")
                .add("migrated")
                .add("cv")
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
