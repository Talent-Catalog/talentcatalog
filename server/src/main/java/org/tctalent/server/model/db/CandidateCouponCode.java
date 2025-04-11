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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Represents a candidate associated with an {@link OfferToAssist} together with an optional coupon
 * code associated with the candidate.
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "candidate_coupon_code")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_coupon_code_id_seq", allocationSize = 1)
@NoArgsConstructor
public class CandidateCouponCode extends AbstractDomainObject<Long> {

    /**
     * Candidate associated with an {@link OfferToAssist}
     */
    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @NonNull
    private Candidate candidate;

    /**
     * Optional coupon code associated with candidate.
     */
    @Nullable
    private String couponCode;

    @ManyToOne
    @JoinColumn(name = "offer_to_assist_id")
    private OfferToAssist offerToAssist;
}

