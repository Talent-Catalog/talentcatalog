package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.request.attachment.UpdateCandidateAttachmentRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.Map;

//import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
//import org.tbbtalent.server.request.note.UpdateCandidateAttachmentRequest;

@RestController()
@RequestMapping("/api/admin/candidate-attachment")
public class CandidateAttachmentAdminApi {

    private final CandidateAttachmentService candidateAttachmentService;
    private final UserContext userContext;

    @Autowired
    public CandidateAttachmentAdminApi(CandidateAttachmentService candidateAttachmentService,
                                       UserContext userContext) {
        this.candidateAttachmentService = candidateAttachmentService;
        this.userContext = userContext;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateAttachmentsRequest request) {
        Page<CandidateAttachment> candidateAttachments = this.candidateAttachmentService.searchCandidateAttachments(request);
        User user = userContext.getLoggedInUser();
        if (user.getRole() == Role.admin || user.getRole() == Role.sourcepartneradmin) {
            return candidateAttachmentDto().buildPage(candidateAttachments);
        } else {
            return candidateAttachmentReadOnlyDto().buildPage(candidateAttachments);
        }
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
                .add("updatedBy", userDto())
                .add("updatedDate")
                ;
    }

    private DtoBuilder candidateAttachmentReadOnlyDto() {
        return new DtoBuilder()
                .add("id")
                .add("type")
                .add("fileType")
                .add("adminOnly")
                .add("migrated")
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
