package org.tbbtalent.server.request.language;

public class CreateCandidateLanguageRequest {

    private Long languageId;
    private Long writtenLevelId;
    private Long spokenLevelId;

    public Long getLanguageId() { return languageId; }

    public void setLanguage(Long languageId) { this.languageId = languageId; }

    public Long getWrittenLevelId() { return writtenLevelId; }

    public void setWrittenLevelId(Long writtenLevelId) { this.writtenLevelId = writtenLevelId; }

    public Long getSpokenLevelId() { return spokenLevelId; }

    public void setSpokenLevelId(Long spokenLevelId) { this.spokenLevelId = spokenLevelId;
    }
}
