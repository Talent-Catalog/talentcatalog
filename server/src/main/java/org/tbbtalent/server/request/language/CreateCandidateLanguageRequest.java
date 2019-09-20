package org.tbbtalent.server.request.language;

public class CreateCandidateLanguageRequest {

    private Long languageId;
    private Long readWriteId;
    private Long speakId;

    public Long getLanguageId() { return languageId; }

    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public Long getReadWriteId() { return readWriteId; }

    public void setReadWriteId(Long readWriteId) { this.readWriteId = readWriteId; }

    public Long getSpeakId() { return speakId; }

    public void setSpeakId(Long speakId) { this.speakId = speakId;
    }
}
