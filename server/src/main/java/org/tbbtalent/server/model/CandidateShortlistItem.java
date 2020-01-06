package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "candidate_shortlist_item")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_shortlist_item_id_seq", allocationSize = 1)
public class CandidateShortlistItem extends AbstractAuditableDomainObject<Long>  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_search_id")
    private SavedSearch savedSearch;

    private String comment;
    @Enumerated(EnumType.STRING)
    private ShortlistStatus shortlistStatus;

    public CandidateShortlistItem() {
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ShortlistStatus getShortlistStatus() {
        return shortlistStatus;
    }

    public void setShortlistStatus(ShortlistStatus shortlistStatus) {
        this.shortlistStatus = shortlistStatus;
    }
}
