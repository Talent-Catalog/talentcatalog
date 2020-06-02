package org.tbbtalent.server.request.attachment;

public class UpdateCandidateAttachmentRequest {

    private Long id;
    private String name;
    private String location;
    private Boolean cv;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getCv() { return cv; }

    public void setCv(Boolean cv) { this.cv = cv; }
}

