package org.tbbtalent.server.request.candidate.language;

import java.util.List;

public class UpdateCandidateLanguagesRequest {

    private List<UpdateCandidateLanguageRequest> updates;

    public List<UpdateCandidateLanguageRequest> getUpdates() {
        return updates;
    }

    public void setUpdates(List<UpdateCandidateLanguageRequest> updates) {
        this.updates = updates;
    }
}


