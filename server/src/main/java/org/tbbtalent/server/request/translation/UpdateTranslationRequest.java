package org.tbbtalent.server.request.translation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateTranslationRequest {

    @NotNull
    private Long translatedId;
    @NotBlank
    private String translatedName;

    public Long getTranslatedId() {
        return translatedId;
    }

    public void setTranslatedId(Long translatedId) {
        this.translatedId = translatedId;
    }

    public String getTranslatedName() {
        return translatedName;
    }

    public void setTranslatedName(String translatedName) {
        this.translatedName = translatedName;
    }
}
