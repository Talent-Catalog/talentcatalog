package org.tbbtalent.server.request.language;

public class CreateLanguageRequest {

    private String name;
    private String readWrite;
    private String speak;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getReadWrite() { return readWrite; }

    public void setReadWrite(String readWrite) { this.readWrite = readWrite; }

    public String getSpeak() { return speak; }

    public void setSpeak(String speak) { this.speak = speak; }
}
