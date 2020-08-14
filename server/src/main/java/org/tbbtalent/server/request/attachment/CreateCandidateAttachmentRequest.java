package org.tbbtalent.server.request.attachment;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.tbbtalent.server.model.db.AttachmentType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCandidateAttachmentRequest {

    private Long candidateId;

    @Enumerated(EnumType.STRING)
    private AttachmentType type;
    
    private String name;
    
    /**
     * Currently this is the file suffix - eg pdf, docx, jpg, etc
     */
    private String fileType;

    /**
     * For links {@link AttachmentType#link} and 
     * Google docs {@link AttachmentType#googlefile}, the associated url.
     * For S3 files {@link AttachmentType#file}, it is the unique filename
     * generated on S3.
     */
    private String location; 
    
    private Boolean cv;

    /**
     * Only used by attachments stored on S3
     */
    private String folder;

}

