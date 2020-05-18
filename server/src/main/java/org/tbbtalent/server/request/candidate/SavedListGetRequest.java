package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.request.SearchRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class SavedListGetRequest extends SearchRequest {
    @NotNull
    private Long savedListId;

    public SavedListGetRequest() {
    }
}
