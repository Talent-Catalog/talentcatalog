package org.tbbtalent.server.request.translation;

import org.tbbtalent.server.request.SearchRequest;

import javax.validation.constraints.NotNull;

public class SearchTranslationRequest extends SearchRequest {

    @NotNull
    private String objectType;

    @NotNull
    private String systemLanguage;

    public String getObjectType() { return objectType; }

    public void setObjectType(String objectType) { this.objectType = objectType; }

    public String getSystemLanguage() { return systemLanguage; }

    public void setSystemLanguage(String systemLanguage) { this.systemLanguage = systemLanguage; }
}

