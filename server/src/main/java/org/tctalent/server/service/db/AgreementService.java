/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.Agreement;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.TermsType;

/**
 * Service responsible for recording and evaluating candidate agreements.
 *
 * @author sadatmalik
 */
public interface AgreementService {

    /**
     * Records candidate acceptance of terms with a given counterparty.
     * <p>
     * If an active agreement of the same counterparty type already exists for the candidate, it is
     * ended before the new agreement is created.
     * </p>
     *
     * @param candidate Candidate accepting terms
     * @param counterparty Agreement counterparty
     * @param termsInfoId Accepted TermsInfo id (version specific)
     * @return Persisted agreement
     */
    @NonNull
    Agreement recordAgreement(@NonNull Candidate candidate,
                              @NonNull Counterparty counterparty,
                              @NonNull String termsInfoId);

    /**
     * Returns true if candidate must accept current terms for the given counterparty before
     * proceeding.
     *
     * @param candidate Candidate
     * @param counterparty Counterparty
     * @param termsType Terms category to resolve current version from
     * @return True when acceptance is required
     */
    boolean needsAcceptance(@NonNull Candidate candidate,
                            @NonNull Counterparty counterparty,
                            @NonNull TermsType termsType);
}
