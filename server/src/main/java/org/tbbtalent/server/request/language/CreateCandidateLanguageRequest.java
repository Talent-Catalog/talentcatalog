package org.tbbtalent.server.request.language;

public class CreateCandidateLanguageRequest {

    private Long languageId;
    private String readWrite;
    private String speak;

    public Long getLanguageId() { return languageId; }

    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public String getReadWrite() { return readWrite; }

    public void setReadWrite(String readWrite) { this.readWrite = readWrite; }

    public String getSpeak() { return speak; }

    public void setSpeak(String speak) { this.speak = speak; }
}
