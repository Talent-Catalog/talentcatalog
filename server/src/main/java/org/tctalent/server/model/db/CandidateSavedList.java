/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import jakarta.persistence.*;

/**
 * A candidate can appear in multiple saved lists.
 * Similarly a saved list contains multiple candidates.
 * This entity represents this Many to Many relationship between Candidates and
 * SavedLists.
 * Each entity record  records the fact that a candidate is associated with a
 * saved list.
 * <p/>
 * It is expressed as an entity so that it can have its own attributes
 * namely {@link #contextNote}.
 * <p/>
 * See
 * https://www.baeldung.com/jpa-many-to-many
 * https://thorben-janssen.com/many-relationships-additional-properties/
 * https://stackoverflow.com/questions/52648330/spring-data-jpa-manytomany-relationship-with-extra-column
 * <p/>
 * Note that it is not explicitly stated in the above links (although it is
 * coded in the GitHub source of the Baeldung example) but this object
 * should also have equals and hashCode implementations which are
 * uniquely defined by the {@link CandidateSavedListKey} id.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "candidate_saved_list")
public class CandidateSavedList {

    @EqualsAndHashCode.Include
    @EmbeddedId
    CandidateSavedListKey id;

    @ManyToOne
    @MapsId("candidateId")
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @MapsId("savedListId")
    @JoinColumn(name = "saved_list_id")
    private SavedList savedList;

    private String contextNote;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shareable_cv_attachment_id")
    private CandidateAttachment shareableCv;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shareable_doc_attachment_id")
    private CandidateAttachment shareableDoc;

    public CandidateSavedList() {
    }

    public CandidateSavedList(Candidate candidate, SavedList savedList) {
        this.candidate = candidate;
        this.savedList = savedList;
        this.id = new CandidateSavedListKey(candidate.getId(), savedList.getId());
    }
}
