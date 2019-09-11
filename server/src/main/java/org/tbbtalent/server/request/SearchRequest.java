package org.tbbtalent.server.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class SearchRequest {
    private Integer pageSize;
    private Integer pageNumber;
    private Sort.Direction sortDirection;
    private String[] sortFields;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Sort.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Sort.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String[] getSortFields() {
        return sortFields;
    }

    public void setSortFields(String[] sortFields) {
        this.sortFields = sortFields;
    }

    public PageRequest getPageRequest() {
        if (sortFields == null) {
            return PageRequest.of(
                    pageNumber != null ? pageNumber : 0,
                    pageSize != null ? pageSize : 25);
        } else {
            return PageRequest.of(
                    pageNumber != null ? pageNumber : 0,
                    pageSize != null ? pageSize : 25,
                    sortDirection != null ? sortDirection : Sort.Direction.ASC,
                    sortFields);
        }
    }
}
