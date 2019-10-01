package org.tbbtalent.server.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "search_join")
@SequenceGenerator(name = "seq_gen", sequenceName = "search_join_id_seq", allocationSize = 1)
public class SearchJoin extends AbstractAuditableDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_id")
    private SavedSearch savedSearch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_search_id")
    private SavedSearch childSavedSearch;

    private SearchType searchType;

    public SearchJoin() {
    }

    public SavedSearch getSavedSearch() {
        return savedSearch;
    }

    public void setSavedSearch(SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
    }

    public SavedSearch getChildSavedSearch() {
        return childSavedSearch;
    }

    public void setChildSavedSearch(SavedSearch childSavedSearch) {
        this.childSavedSearch = childSavedSearch;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }
}
