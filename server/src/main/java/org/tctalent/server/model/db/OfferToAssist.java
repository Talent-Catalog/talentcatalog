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

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_exam_id_seq", allocationSize = 1)
@NoArgsConstructor
public class OfferToAssist extends AbstractAuditableDomainObject<Long> {

    /**
     * Unique public ID associated with this OfferToAssist
     */
    private String publicId;

    /**
     * Candidates and optional coupon codes associated with offer.
     */
    private List<CandidateCoupon> candidateCoupons = new ArrayList<>();

    //TODO JC May not be needed - replace with reference to partner entity?
    private String serviceProviderId;

    /**
     * The reason for expressing interest in the candidate.
     */
    private CandidateAssistanceType reason;

    /**
     * Optional additional notes provided by the partner offering assistance.
     */
    private String additionalNotes;

}
