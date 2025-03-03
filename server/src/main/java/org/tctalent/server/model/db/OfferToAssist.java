/*
 * Copyright (c) 2025 Talent Catalog.
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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.anonymization.model.CandidateAssistanceType;

/**
 * An offer to assist candidates.
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "offer_to_assist")
@SequenceGenerator(name = "seq_gen", sequenceName = "offer_to_assist_id_seq", allocationSize = 1)
@NoArgsConstructor
public class OfferToAssist extends AbstractAuditableDomainObject<Long> {

    /**
     * Optional additional notes provided by the partner offering assistance.
     */
    private String additionalNotes;

    /**
     * Candidates and optional coupon codes associated with offer.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "offerToAssist", cascade = CascadeType.MERGE)
    private List<CandidateCouponCode> candidateCouponCodes = new ArrayList<>();

    /**
     * Unique public ID associated with this OfferToAssist
     */
    private String publicId;

    /**
     * The reason for expressing interest in the candidate.
     */
    @Enumerated(EnumType.STRING)
    @Nullable
    private CandidateAssistanceType reason;

    //TODO JC May not be needed - replace with reference to partner entity?
    private transient String serviceProviderId;

}
