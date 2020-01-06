package org.tbbtalent.server.request.candidate.language;

public class CreateCandidateLanguageRequest {

    private Long id;
    private Long writtenLevelId;
    private Long spokenLevelId;

    public Long getId() { return id; }

    public void setId(Long languageId) { this.id = languageId; }

    public Long getWrittenLevelId() { return writtenLevelId; }

    public void setWrittenLevelId(Long writtenLevelId) { this.writtenLevelId = writtenLevelId; }

    public Long getSpokenLevelId() { return spokenLevelId; }

    public void setSpokenLevelId(Long spokenLevelId) { this.spokenLevelId = spokenLevelId;
    }
}
