package org.tbbtalent.server.request.candidate;

import java.util.List;

import org.tbbtalent.server.model.ShortlistStatus;
import org.tbbtalent.server.request.SearchRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class SavedSearchRunRequest extends SearchRequest {
    private Long savedSearchId;
    private List<ShortlistStatus> shortlistStatus;

    public SavedSearchRunRequest() {
    }
}
