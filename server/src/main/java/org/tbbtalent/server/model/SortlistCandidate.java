package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "shortlist_candidate")
@SequenceGenerator(name = "seq_gen", sequenceName = "shortlist_candidate_id_seq", allocationSize = 1)
public class SortlistCandidate extends AbstractAuditableDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_id")
    private SavedSearch savedSearch;

    @Enumerated(EnumType.STRING)
    private ShortlistStatus shortlistStatus;

    private String comment;

    public SortlistCandidate() {
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public SavedSearch getSavedSearch() {
        return savedSearch;
    }

    public void setSavedSearch(SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
    }

    public ShortlistStatus getShortlistStatus() {
        return shortlistStatus;
    }

    public void setShortlistStatus(ShortlistStatus shortlistStatus) {
        this.shortlistStatus = shortlistStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
