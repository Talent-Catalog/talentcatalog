package org.tbbtalent.server.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request that includes paging and sorting fields.
 */
@Getter @Setter @ToString
public class PagedSearchRequest {
    private Integer pageSize;
    private Integer pageNumber;
    private Sort.Direction sortDirection;
    private String[] sortFields;

    public PagedSearchRequest() {
    }

    public PagedSearchRequest(Sort.Direction sortDirection, String[] sortFields) {
        this.sortDirection = sortDirection;
        this.sortFields = sortFields;
    }

    public PageRequest getPageRequest() {
        if (sortFields == null) {
            return getPageRequestWithoutSort();
        } else {
            return PageRequest.of(
                    pageNumber != null ? pageNumber : 0,
                    pageSize != null ? pageSize : 25,
                    Sort.by(sortDirection, sortFields));
        }
    }

    public PageRequest getPageRequestWithoutSort() {
        return PageRequest.of(
                pageNumber != null ? pageNumber : 0,
                pageSize != null ? pageSize : 25);

    }

}
