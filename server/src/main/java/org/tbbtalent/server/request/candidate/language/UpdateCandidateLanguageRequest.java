package org.tbbtalent.server.request.candidate.language;

public class UpdateCandidateLanguageRequest {

    private Long languageId;
    private Long writtenLevelId;
    private Long spokenLevelId;

    public Long getLanguageId() { return languageId; }

    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public Long getWrittenLevelId() { return writtenLevelId; }

    public void setWrittenLevelId(Long writtenLevelId) { this.writtenLevelId = writtenLevelId; }

    public Long getSpokenLevelId() { return spokenLevelId; }

    public void setSpokenLevelId(Long spokenLevelId) { this.spokenLevelId = spokenLevelId; }
}
