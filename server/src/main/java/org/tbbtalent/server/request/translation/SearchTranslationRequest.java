package org.tbbtalent.server.request.translation;

import org.tbbtalent.server.request.SearchRequest;

import javax.validation.constraints.NotNull;

public class SearchTranslationRequest extends SearchRequest {

    @NotNull
    private String systemLanguage;

    public String getSystemLanguage() { return systemLanguage; }

    public void setSystemLanguage(String systemLanguage) { this.systemLanguage = systemLanguage; }
}

