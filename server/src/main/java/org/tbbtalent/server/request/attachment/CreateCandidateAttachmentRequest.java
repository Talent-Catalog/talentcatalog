package org.tbbtalent.server.request.attachment;

import org.tbbtalent.server.model.AttachmentType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class CreateCandidateAttachmentRequest {

    private Long candidateId;
    @Enumerated(EnumType.STRING)
    private AttachmentType type;
    private String name;
    private String fileType;
    private String folder;
    private String location; // Used for creating link attachments on admin

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public AttachmentType getType() {
        return type;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

