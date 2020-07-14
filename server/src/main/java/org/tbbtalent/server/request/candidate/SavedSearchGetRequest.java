package org.tbbtalent.server.request.candidate;

import java.util.List;

import org.tbbtalent.server.model.ReviewStatus;
import org.tbbtalent.server.request.PagedSearchRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class SavedSearchGetRequest extends PagedSearchRequest {
    private List<ReviewStatus> reviewStatusFilter;
}
