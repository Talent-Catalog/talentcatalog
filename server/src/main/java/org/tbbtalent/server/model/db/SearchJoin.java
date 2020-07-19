/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "search_join")
@SequenceGenerator(name = "seq_gen", sequenceName = "search_join_id_seq", allocationSize = 1)
public class SearchJoin extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_id")
    private SavedSearch savedSearch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_search_id")
    private SavedSearch childSavedSearch;

    @Enumerated(EnumType.STRING)
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
