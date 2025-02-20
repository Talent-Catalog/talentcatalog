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

package org.tctalent.server.service.db;


import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.AbstractOpportunity;

/**
 * We add an audit stamp to the user-entered value for Next Step in SF and TC Candidate and Job Opps.
 * To guard against future divergence, this is a lightweight service to handle that processing.
 *
 * @author samschlicht
 */
public interface NextStepProcessingService {

    /**
     * If requested Next Step differs from current, provides an audit stamped version.
     * @param opp the Opp whose Next Step may be updated
     * @param requestedNextStep the user-entered value, prior to audit stamp
     * @return processed Next Step String
     */
    String processNextStep(@Nullable AbstractOpportunity opp, @Nullable String requestedNextStep);

}
