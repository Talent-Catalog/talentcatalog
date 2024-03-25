/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import javax.persistence.criteria.Predicate;
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
        return (helpLink, query, builder) -> {
            Predicate conjunction = builder.conjunction();
            query.distinct(true);

            if (request.getCountryId() != null){
                conjunction.getExpressions().add(builder.equal(helpLink.get("country").get("id"), request.getCountryId()));
            }

            return conjunction;
        };
    }
}
