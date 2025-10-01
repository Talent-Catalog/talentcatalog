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

/**
 * Every candidate can only be in one status at a time.
 * <p/>
 * Only candidates with CandidateStatus.active are made visible to potential employers
 */
public enum CandidateStatus {

    /**
     * The candidate's data is ready to be shared with prospective employers.
     */
    active,

    /**
     * The candidate has found employment themselves.
     */
    autonomousEmployment,

    /**
     * Candidate has been deleted. (The status of the candidate's corresponding User object should
     * also be set to deleted - ie {@link Status#deleted}
     */
    deleted,

    /**
     * Candidate has started registration but has not submitted - ie they are still in the
     * middle of completing their registration.
     */
    draft,

    /**
     * Candidate is no longer looking for placement through the TC.
     */
    employed,

    /**
     * Candidate's registration is not complete enough to be considered active.
     */
    incomplete,

    /**
     * The candidate is not eligible for support on the TC.
     */
    ineligible,

    /**
     * Candidate has completed registration, but the registration has not yet been reviewed.
     */
    pending,

    /**
     * The candidate has independently relocated to a country with a durable solution.
     * They therefore no longer need our services and should not be considered active.
     */
    relocatedIndependently,

    /**
     * The candidate cannot be contacted.
     */
    unreachable,

    /**
     * The candidate has requested to be withdrawn from consideration.
     */
    withdrawn,


}
