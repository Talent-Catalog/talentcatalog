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

package org.tctalent.server.repository.db;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

/**
 * This is used in the HelpLinkAPI fetch method for fetching help for a user.
 * <p/>
 * It does not process the keyword field of {@link SearchHelpLinkRequest}.
 * That is only used by {@link HelpLinkSettingsSpecification}
 */
public class HelpLinkFetchSpecification {

    public static Specification<HelpLink> buildSearchQuery(final SearchHelpLinkRequest request) {
        return (helpLink, query, cb) -> {
            if (query == null) {
                throw new IllegalArgumentException("HelpLinkFetchSpecification.CriteriaQuery should not be null");
            }
            query.distinct(true);

            Predicate conjunction = cb.conjunction();

            if (request.getCountryId() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(helpLink.get("country").get("id"), request.getCountryId()));
            }

            if (request.getCaseStage() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(helpLink.get("caseStage"), request.getCaseStage()));
            }

            if (request.getFocus() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(helpLink.get("focus"), request.getFocus()));
            }

            if (request.getJobStage() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(helpLink.get("jobStage"), request.getJobStage()));
            }

            if (request.getNextStepName() != null){
                conjunction = cb.and(conjunction,
                    cb.equal(helpLink.get("nextStepInfo").get("nextStepName"),
                        request.getNextStepName()));
            }

            return conjunction;
        };
    }
}
