package org.tbbtalent.server.request.translation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateTranslationRequest {

    @NotNull
    private Long id;
    @NotBlank
    private String type;
    @NotBlank
    private String language;
    @NotBlank
    private String translatedName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTranslatedName() {
        return translatedName;
    }

    public void setTranslatedName(String translatedName) {
        this.translatedName = translatedName;
    }
}
